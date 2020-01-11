path = commandArgs(trailingOnly = TRUE)[1]
target = commandArgs(trailingOnly = TRUE)[2]
method = commandArgs(trailingOnly = TRUE)[3]
fset = commandArgs(trailingOnly = TRUE)[4]
undersam = commandArgs(trailingOnly = TRUE)[5]
opfunc = commandArgs(trailingOnly = TRUE)[6]
outpath = commandArgs(trailingOnly = TRUE)[7]

source('classify.R')
library(data.table)
df = fread(path, data.table=F)

# turn nextCost_b, nextLOS_b into binary
df[df$nextCost_b <= 3, 'nextCost_b'] = 0
df[df$nextCost_b > 3, 'nextCost_b'] = 1
df[df$nextLOS_b <= 4, 'nextLOS_b'] = 0
df[df$nextLOS_b > 4, 'nextLOS_b'] = 1

# define feature sets
fdemo = c('agyradm', 'gender', 'race_grp')
fclos = c('cost', 'LOS')
fadmin = c('schedule', 'srcsite', 'srcroute', 'msdrg_severity_ill', 'type_care', 'sameday', 'oshpd_destination')
fcom = names(df)[grepl('ch_com', names(df))]
fcum = c('coms', 'cons', 'er6m', 'adms')
fres = c('thirtyday', 'nextCost_b', 'nextLOS_b')

# remove unused variables
df = df[,!names(df) %in% c('admitDT', 'dischargeDT', 'nextCost', 'nextLOS','LOS_b','cost_b', c(fres[fres != target]))]

# turn variables into factor
facCol = c(target,'type_care','gender','srcsite','srcroute','schedule','oshpd_destination','race_grp','msdrg_severity_ill','sameday', 'merged', fcom)
for (col in facCol) df[,col] = as.factor(df[,col])

# undersampling
undersam = (!is.na(undersam) & undersam == 'T')

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
allfeats = c(fdemo, fclos, fadmin, fcom, fcum)
#for (i in eval(parse(text=paste('c(',fset,')',sep=''))))
#{
  #feats = c(target)
  #feats = c(target, 'pnextLOS_b')
  #feats = c(target, 'nextLOS_b', 'pnextLOS_b')
  #for (j in 1:i) feats=c(feats, fsets[[j]])
  feats = c(target, allfeats[eval(parse(text=paste('c(',fset,')',sep='')))])
  #message('feature set 1:', i)
  if (grepl('dt', method)) mlmethod = decisiontree
  if (grepl('lr', method)) mlmethod = logistic
  if (grepl('ada', method)) mlmethod = adaboost
  result = mlmethod(df[,feats], target, opfunc, undersam)
  if (!is.na(outpath)) write.csv(cbind(PID=df$PID, predict=result$result), outpath, quote=F, row.names=F)
#}
