path = commandArgs(trailingOnly = TRUE)[1]
target = commandArgs(trailingOnly = TRUE)[2]
method = commandArgs(trailingOnly = TRUE)[3]

source('classify.R')
library(data.table)
df = fread(path, data.table=F)

# remove unused variables
df = df[,!names(df) %in% c('admitDT', 'dischargeDT', 'PID', 'nextCost', 'nextLOS','LOS_b','cost_b')]

# turn cost_b, nextCost_b, LOS_b, nextLOS_b into binary
#df[df$cost_b <= 3, 'cost_b'] = 0
#df[df$cost_b > 3, 'cost_b'] = 1
df[df$nextCost_b <= 3, 'nextCost_b'] = 0
df[df$nextCost_b > 3, 'nextCost_b'] = 1
#df[df$LOS_b <= 4, 'LOS_b'] = 0
#df[df$LOS_b > 4, 'LOS_b'] = 1
df[df$nextLOS_b <= 4, 'nextLOS_b'] = 0
df[df$nextLOS_b > 4, 'nextLOS_b'] = 1

fdemo = c('agyradm', 'gender', 'race_grp')
fclos = c('cost', 'LOS')
fadmin = c('schedule', 'srcsite', 'srcroute', 'msdrg_severity_ill', 'type_care', 'sameday', 'oshpd_destination')
fcom = names(df)[grepl('ch_com', names(df))]
fcum = c('coms', 'cons', 'er6m', 'adms')
fres = c('thirtyday', 'nextCost_b', 'nextLOS_b')

# turn variables into factor
facCol = c('thirtyday', 'nextLOS_b','nextCost_b','type_care','gender','srcsite','srcroute','schedule','oshpd_destination','race_grp','msdrg_severity_ill','sameday', 'merged', fcom)
for (col in facCol) df[,col] = as.factor(df[,col])

# flatten categorical columns (for svm)
#flatCol = c('type_care','gender','srcsite','srcroute','schedule','oshpd_destination','race_grp','msdrg_severity_ill','sameday')
#for (col in flatCol)
#{
#  newCols = class.ind(df[,col])
#  for (i in 1:ncol(newCols)) colnames(newCols)[i] = paste(col,colnames(newCols)[i],sep='')
#  df = df[,!names(df) %in% col]
#  df = cbind(df, newCols)
#}

fsets = list(fdemo, fclos, fadmin, fcom, fcum)
for (i in 1:length(fsets))
{
  feats = c(fsets[[i]], target)
  #for (j in 1:i) feats=c(feats, fsets[[j]])
  message('feature set', i)
  if (grepl('dt', method)) decisiontree(df[,feats], target)
  if (grepl('lr', method)) logistic(df[,feats], target)
  if (grepl('ada', method)) adaboost(df[,feats], target)
}
