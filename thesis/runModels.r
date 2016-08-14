path = commandArgs(trailingOnly = TRUE)[1]
pathmodels = commandArgs(trailingOnly = TRUE)[2]

library(data.table)
library(rpart)
df = fread(path, data.table=F)

# remove unused variables
df = df[,!names(df) %in% c('admitDT', 'dischargeDT', 'PID', 'nextCost', 'nextLOS','LOS_b','cost_b', 'nextCost_b', 'nextLOS_b')]

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

models = readRDS(pathmodels)

pids = unique(df$PID)
for (pid in pids)
{
  rows = df[df$PID == pid,]
  for (i in 1:nrows(rows))
  {
    for (col in allfeats)
    {
      vals = as.list(rows[1:i, col])
      names(vals) = sapply(1:i, function(x) paste('X', x, sep=''))
      prob = predict(models[[col]][[i]], vals)
    }
  }
}
