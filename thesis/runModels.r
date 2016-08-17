path = commandArgs(trailingOnly = TRUE)[1]
pathmodels = commandArgs(trailingOnly = TRUE)[2]
fset = commandArgs(trailingOnly = TRUE)[3]
datarange = commandArgs(trailingOnly = TRUE)[4]

library(data.table)
library(rpart)
library(pROC)

message('fset ', fset)
message('Loading models...')
models = readRDS(pathmodels)
maxlen = length(models[[1]])
message('Done loading models')

df = fread(path, data.table=F)

# take a subset of data
if (length(commandArgs(trailingOnly = TRUE)) >= 4) df = df[eval(parse(text=paste('c(',datarange,')',sep=''))), ]

# remove unused variables
df = df[,!names(df) %in% c('admitDT', 'dischargeDT', 'nextCost', 'nextLOS','LOS_b','cost_b', 'nextCost_b', 'nextLOS_b')]

# define feature sets
fdemo = c('agyradm', 'gender', 'race_grp')
fclos = c('cost', 'LOS')
fadmin = c('schedule', 'srcsite', 'srcroute', 'msdrg_severity_ill', 'type_care', 'sameday', 'oshpd_destination', 'merged')
fcom = names(df)[grepl('ch_com', names(df))]
fcum = c('coms', 'cons', 'er6m', 'adms', 'lace')

allfeats = c(fdemo, fclos, fadmin, fcom, fcum)
if (length(commandArgs(trailingOnly = TRUE)) >= 3) allfeats = allfeats[eval(parse(text=paste('c(',fset,')',sep='')))]

# turn variables into factor
facCol = c('thirtyday', 'type_care','gender','srcsite','srcroute','schedule','oshpd_destination','race_grp','msdrg_severity_ill','sameday', 'merged', fcom)
for (col in facCol) df[,col] = as.factor(df[,col])

pids = unique(df$PID)
probs = c()
ans = c()
count = 1
for (pid in pids)
{
  if ((count %% floor(length(pids) / 100)) == 0) message('Running ', count * 100 / length(pids), '%')
  count = count + 1
  rows = df[df$PID == pid,]
  # skip patients less than 4 records
  if (nrow(rows) < 4) next
  for (i in 4:nrow(rows))
  {
    thisprobs = c()
    for (col in allfeats)
    {
      start = max(1, i - maxlen + 1)
      vals = as.list(rows[start:i, col])
      names(vals) = sapply(1:length(vals), function(x) paste('X', x, sep=''))
      tryCatch({
        prob = predict(models[[col]][[length(vals)]], vals)[2]
        thisprobs = c(thisprobs, prob)
      }, error = function(err)
      {
        message('Error in column ', col, ', pid ', pid, '. Probability skipped\n', err)
      })
    }
    if (length(thisprobs) == 0) thisprobs = c(0)
    probs = c(probs, mean(thisprobs))
    ans = c(ans, rows[i, 'thirtyday'])
  }
}

message('auc ', auc(ans, probs))
