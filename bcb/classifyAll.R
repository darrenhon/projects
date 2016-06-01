source('classify.R')
library(data.table)

path = commandArgs(trailingOnly = TRUE)[1]
df = fread(path, data.table=F)
df = df[,!names(df) %in% c('admitDT', 'dischargeDT', 'PID', 'nextCost', 'nextLOS','LOS_b','cost_b')]

# turn variables into factor
facCol = c('thirtyday','nextLOS_b','nextCost_b','type_care','gender','srcsite','srcroute','schedule','oshpd_destination','race_grp','msdrg_severity_ill','sameday','diag_p_elix')
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

# thrityday
df2 = df[,!names(df) %in% c('nextCost_b','nextLOS_b')]
ann(df2, 'thirtyday', 10, T)
logistic(df2, 'thirtyday')
naivebayes(df2, 'thirtyday', T)
decisiontree(df2, 'thirtyday', T)

# nextCost_b
df2 = df[,!names(df) %in% c('thirtyday','nextLOS_b')]
ann(df2, 'nextCost_b', 10)
multinomlogistic(df2, 'nextCost_b')
naivebayes(df2, 'nextCost_b')
decisiontree(df2, 'nextCost_b')

# nextLOS_b
df2 = df[,!names(df) %in% c('nextCost_b','thirtyday')]
ann(df2, 'nextLOS_b', 10)
multinomlogistic(df2, 'nextLOS_b')
naivebayes(df2, 'nextLOS_b')
decisiontree(df2, 'nextLOS_b')
