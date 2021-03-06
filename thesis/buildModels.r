target = commandArgs(trailingOnly = TRUE)[1]
path = commandArgs(trailingOnly = TRUE)[2]
maxlen = commandArgs(trailingOnly = TRUE)[3]
pathflip = commandArgs(trailingOnly = TRUE)[4]
pathmodel = commandArgs(trailingOnly = TRUE)[5]

library(data.table)
library(rpart)
df = fread(path, data.table=F)

# remove unused variables
todelete = c('admitDT', 'dischargeDT', 'PID', 'nextCost', 'nextLOS','LOS_b','cost_b', 'thirtyday', 'nextLOS_b', 'nextCost_b')
todelete = todelete[todelete != target]
df = df[,!names(df) %in% todelete]

# define feature sets
fdemo = c('agyradm', 'gender', 'race_grp')
fclos = c('cost', 'LOS')
fadmin = c('schedule', 'srcsite', 'srcroute', 'msdrg_severity_ill', 'type_care', 'sameday', 'oshpd_destination', 'merged')
fcom = names(df)[grepl('ch_com', names(df))]
fcum = c('coms', 'cons', 'er6m', 'adms', 'lace')

allfeats = c(fdemo, fclos, fadmin, fcom, fcum, target)

# turn variables into factor
facCol = c(target, 'type_care','gender','srcsite','srcroute','schedule','oshpd_destination','race_grp','msdrg_severity_ill','sameday', 'merged', fcom)
for (col in facCol) df[,col] = as.factor(df[,col])

models = as.list(1:length(allfeats))
names(models) = allfeats
for (i in 1:length(allfeats)) models[[i]] = as.list(1:maxlen)

for (col in allfeats)
{
  for (i in 1:maxlen)
  {
    message('building ', col, ' ', i)
    dfflip = fread(paste(pathflip, col, '-', i, '.csv', sep=''), data.table=F)
    if (col %in% facCol)
    {
      for (j in 1:ncol(dfflip)) dfflip[,j] = as.factor(dfflip[,j])
    }
    dfflip[,'Y'] = as.factor(dfflip[,'Y'])
    dt = rpart(Y~., dfflip, control = rpart.control(cp=0.00001))
    dt = prune(dt, cp=dt$cptable[which.min(dt$cptable[,"xerror"]),"CP"])
    models[[col]][[i]] = dt
  }
}

saveRDS(models, pathmodel)
