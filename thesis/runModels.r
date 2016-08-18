path = commandArgs(trailingOnly = TRUE)[1]
pathmodels = commandArgs(trailingOnly = TRUE)[2]
lrmodel = commandArgs(trailingOnly = TRUE)[3]
fset = commandArgs(trailingOnly = TRUE)[4]
datarange = commandArgs(trailingOnly = TRUE)[5]

library(data.table)
library(rpart)
library(pROC)

message('fset ', fset)
message('Loading models...')
lr = readRDS(lrmodel)
models = readRDS(pathmodels)
maxlen = length(models[[1]])
message('Done loading models')

df = fread(path, data.table=F)

# take a subset of data
if (!is.na(datarange)) df = df[eval(parse(text=paste('c(',datarange,')',sep=''))), ]

# remove unused variables
df = df[,!names(df) %in% c('admitDT', 'dischargeDT', 'nextCost', 'nextLOS','LOS_b','cost_b', 'nextCost_b', 'nextLOS_b')]

# define feature sets
fdemo = c('agyradm', 'gender', 'race_grp')
fclos = c('cost', 'LOS')
fadmin = c('schedule', 'srcsite', 'srcroute', 'msdrg_severity_ill', 'type_care', 'sameday', 'oshpd_destination', 'merged')
fcom = names(df)[grepl('ch_com', names(df))]
fcum = c('coms', 'cons', 'er6m', 'adms', 'lace')

allfeats = c(fdemo, fclos, fadmin, fcom, fcum)
if (!is.na(fset)) allfeats = allfeats[eval(parse(text=paste('c(',fset,')',sep='')))]

# turn variables into factor
facCol = c('thirtyday', 'type_care','gender','srcsite','srcroute','schedule','oshpd_destination','race_grp','msdrg_severity_ill','sameday', 'merged', fcom)
for (col in facCol) df[,col] = as.factor(df[,col])

pids = unique(df$PID)
probs = as.list(rep(NA, length(allfeats) + 1))
names(probs) = c(allfeats, 'lr')
ans = c()
count = 1
for (pid in pids)
{
  if ((count %% floor(length(pids) / 100)) == 0) message('Running ', count * 100 / length(pids), '%')
  count = count + 1
  rows = df[df$PID == pid,]
  # skip patients less than 4 records
  #if (nrow(rows) < 4) next
  #for (i in 4:nrow(rows))
  for (i in 1:nrow(rows))
  {
    thisprobs = c()
    for (col in allfeats)
    {
      start = max(1, i - maxlen + 1)
      vals = as.list(rows[start:i, col])
      names(vals) = sapply(1:length(vals), function(x) paste('X', x, sep=''))
      tryCatch({
        probs[[col]] = c(probs[[col]], predict(models[[col]][[length(vals)]], vals)[2])
      }, error = function(err)
      {
        probs[[col]] <<- c(probs[[col]], 0.3434)
        message('Error in column ', col, ', pid ', pid, '. Use majority prob.\n', err)
      })
    }
    probs[['lr']] = c(probs[['lr']], predict(lr, rows[i,], type='response'))
    ans = c(ans, rows[i, 'thirtyday'])
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
message('auc mean all features probs ', auc(ans, avg))
message('auc lr only ', auc(ans, probs[['lr']]))

message('optimizing auc')
res = optim(rep(1/(length(allfeats) + 1), length(allfeats) + 1), weightedAvgAucNeg, lower=0, upper=1, method='L-BFGS-B', allprobs=probs, ans=ans)
message('auc optimized ', -res$value)
message('weights ', paste(res$par/sum(res$par), collapse=' ,'))
