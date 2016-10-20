target = commandArgs(trailingOnly = TRUE)[1]
path = commandArgs(trailingOnly = TRUE)[2]
pathmodels = commandArgs(trailingOnly = TRUE)[3]
lrmodel = commandArgs(trailingOnly = TRUE)[4]
fset = commandArgs(trailingOnly = TRUE)[5]
datarange = commandArgs(trailingOnly = TRUE)[6]
filterseqlen = commandArgs(trailingOnly = TRUE)[7]
takelastnsym = commandArgs(trailingOnly = TRUE)[8]
weights = commandArgs(trailingOnly = TRUE)[9]

valid = function(arg)
{
  return(!is.na(arg) & arg != 'NA')
}

library(data.table)
library(rpart)
library(pROC)

message('fset ', fset)
message('datarange ', datarange)
message('filterseqlen ', filterseqlen)
message('takelastnsym ', takelastnsym)
message('weights ', weights)

message('Loading data...')
df = fread(path, data.table=F)
message('Done loading data')

message('Loading models...')
lr = readRDS(lrmodel)
models = readRDS(pathmodels)
maxlen = length(models[[1]])
message('Done loading models')

# take a subset of data
if (valid(datarange)) df = df[eval(parse(text=paste('c(',datarange,')',sep=''))), ]

# remove unused variables
todelete = c('admitDT', 'dischargeDT', 'nextCost', 'nextLOS','LOS_b','cost_b', 'thirtyday', 'nextLOS_b', 'nextCost_b')
todelete = todelete[todelete != target]
df = df[,!names(df) %in% todelete]

# define feature sets
fdemo = c('agyradm', 'gender', 'race_grp')
fclos = c('cost', 'LOS')
fadmin = c('schedule', 'srcsite', 'srcroute', 'msdrg_severity_ill', 'type_care', 'sameday', 'oshpd_destination', 'merged')
fcom = names(df)[grepl('ch_com', names(df))]
fcum = c('coms', 'cons', 'er6m', 'adms', 'lace')

allfeats = c(fdemo, fclos, fadmin, fcom, fcum)
if (valid(fset)) allfeats = allfeats[eval(parse(text=paste('c(',fset,')',sep='')))]

# turn variables into factor
facCol = c(target, 'type_care','gender','srcsite','srcroute','schedule','oshpd_destination','race_grp','msdrg_severity_ill','sameday', 'merged', fcom)
for (col in facCol) df[,col] = as.factor(df[,col])

pids = unique(df$PID)
probs = as.list(rep(NA, length(allfeats) + 1))
names(probs) = c(allfeats, 'lr')
ans = c()
count = 1
maj = table(df[,target])['1'] / nrow(df)
for (pid in pids)
{
  if ((count %% floor(length(pids) / 100)) == 0) message('Running ', count * 100 / length(pids), '%')
  count = count + 1
  rows = df[df$PID == pid,]
  # filter sequence length
  if (valid(filterseqlen) & nrow(rows) != filterseqlen)
    next
  # do it for all rows
  #for (i in 1:nrow(rows))
  # skip patients less than 4 records
  #if (nrow(rows) < 4) next
  #for (i in 4:nrow(rows))
  # do it only for the last row
  for (i in c(nrow(rows)))
  {
    thisprobs = c()
    for (col in allfeats)
    {
      start = max(1, i - maxlen + 1)
      vals = rows[start:i, col]
      if (valid(takelastnsym))
        vals = vals[1:min(as.numeric(takelastnsym), length(vals))]
      vals = as.list(vals)
      names(vals) = sapply(1:length(vals), function(x) paste('X', x, sep=''))
      tryCatch({
        probs[[col]] = c(probs[[col]], predict(models[[col]][[length(vals)]], vals)[2])
      }, error = function(err)
      {
        probs[[col]] <<- c(probs[[col]], maj)
        message('Error in column ', col, ', pid ', pid, '. Use majority prob.\n', err)
      })
    }
    probs[['lr']] = c(probs[['lr']], predict(lr, rows[i,], type='response'))
    ans = c(ans, rows[i, target])
  }
}
probs = lapply(probs, function(item) item[!is.na(item)])

weightedAvgAucNeg = function(w, allprobs, ans)
{
  #normalize w
  w = w / sum(w)
  for (i in 1:length(allprobs)) allprobs[[i]] = allprobs[[i]] * w[i]
  waprobs = Reduce('+', allprobs)
  return(-as.numeric(auc(ans, waprobs)))
}

avg = Reduce('+', probs) / length(probs)
message('auc mean all features with lr ', auc(ans, avg))
message('auc lr only ', auc(ans, probs[['lr']]))

if (valid(weights))
{
  weights = eval(parse(text=paste('c(', weights,')',sep='')))
  if (length(weights) == length(allfeats))
  {
    probs = probs[names(probs) != 'lr']
    message('auc without lr from provided weights ', -weightedAvgAucNeg(weights, probs, ans))
  }
  else if (length(weights) == length(allfeats) + 1)
  {
    message('auc with lr from provided weights ', -weightedAvgAucNeg(weights, probs, ans))
  }
} else
{
  message('optimizing auc with lr')
  res = optim(c(rep(0, length(allfeats)), 1), weightedAvgAucNeg, lower=0, upper=1, method='L-BFGS-B', allprobs=probs, ans=ans)
  message('auc optimized ', -res$value)
  message('weights ', paste(res$par/sum(res$par), collapse=' ,'))

  probs = probs[names(probs) != 'lr']
  message('optimizing auc without lr')
  res = optim(rep(1, length(allfeats)), weightedAvgAucNeg, lower=0, upper=1, method='L-BFGS-B', allprobs=probs, ans=ans)
  message('auc optimized ', -res$value)
  message('weights ', paste(res$par/sum(res$par), collapse=' ,'))
}
