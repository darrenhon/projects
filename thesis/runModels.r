path = commandArgs(trailingOnly = TRUE)[1]
pathmodels = commandArgs(trailingOnly = TRUE)[2]

library(data.table)
library(rpart)
library(pROC)

message('Loading models...')
models = readRDS(pathmodels)
maxlen = length(models[[1]])
message('Done loading models')

df = fread(path, data.table=F)[1:10000,]

# remove unused variables
df = df[,!names(df) %in% c('admitDT', 'dischargeDT', 'nextCost', 'nextLOS','LOS_b','cost_b', 'nextCost_b', 'nextLOS_b')]

# define feature sets
fdemo = c('agyradm', 'gender', 'race_grp')
fclos = c('cost', 'LOS')
fadmin = c('schedule', 'srcsite', 'srcroute', 'msdrg_severity_ill', 'type_care', 'sameday', 'oshpd_destination', 'merged')
fcom = names(df)[grepl('ch_com', names(df))]
fcum = c('coms', 'cons', 'er6m', 'adms', 'lace')

allfeats = c(fdemo, fclos, fadmin, fcom, fcum)

# turn variables into factor
facCol = c('thirtyday', 'type_care','gender','srcsite','srcroute','schedule','oshpd_destination','race_grp','msdrg_severity_ill','sameday', 'merged', fcom)
for (col in facCol) df[,col] = as.factor(df[,col])

pids = unique(df$PID)
probs = c()
count = 1
for (pid in pids)
{
  if ((count %% floor(length(pids) / 100)) == 0) message('Running ', count * 100 / length(pids), '%')
  count = count + 1
  rows = df[df$PID == pid,]
  for (i in 1:nrow(rows))
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
    probs = c(probs, mean(thisprobs))
  }
}

message('auc ', auc(df[,'thirtyday'], probs)
