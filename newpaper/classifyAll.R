pathTrain = commandArgs(trailingOnly = TRUE)[1]
pathTest = commandArgs(trailingOnly = TRUE)[2]
target = commandArgs(trailingOnly = TRUE)[3]
method = commandArgs(trailingOnly = TRUE)[4]
fset = commandArgs(trailingOnly = TRUE)[5]
undersam = commandArgs(trailingOnly = TRUE)[6]
opfunc = commandArgs(trailingOnly = TRUE)[7]
outpath = commandArgs(trailingOnly = TRUE)[8]

source('classify.R')
library(data.table)

prepareData <- function(df)
{
  # turn nextCost_b, nextLOS_b into binary
  df[df$nextCost_b <= 3, 'nextCost_b'] = 0
  df[df$nextCost_b > 3, 'nextCost_b'] = 1
  df[df$nextLOS_b <= 4, 'nextLOS_b'] = 0
  df[df$nextLOS_b > 4, 'nextLOS_b'] = 1

  # define feature sets
  fdemo <<- c('agyradm', 'gender', 'race_grp')
  fclos <<- c('cost', 'LOS')
  fadmin <<- c('schedule', 'srcsite', 'srcroute', 'msdrg_severity_ill', 'type_care', 'sameday', 'oshpd_destination')
  fcom <<- names(df)[grepl('ch_com', names(df))]
  fcum <<- c('coms', 'cons', 'er6m', 'adms', 'lace')
  fres <<- c('thirtyday', 'nextCost_b', 'nextLOS_b')

  # remove unused variables
  df = df[,!names(df) %in% c('admitDT', 'dischargeDT', 'nextCost', 'nextLOS','LOS_b','cost_b', c(fres[fres != target]))]

  # turn variables into factor
  facCol = c(target,'type_care','gender','srcsite','srcroute','schedule','oshpd_destination','race_grp','msdrg_severity_ill','sameday', 'merged', fcom)
  for (col in facCol) df[,col] = as.factor(df[,col])
  return(df)
}

dfTrain = prepareData(fread(pathTrain, data.table=F))
dfTest = prepareData(fread(pathTest, data.table=F))

# undersampling
undersam = (!is.na(undersam) & undersam == 'T')

fsets = list(fdemo, fclos, fadmin, fcom, fcum)
allfeats = c(fdemo, fclos, fadmin, fcom, fcum)
feats = c(target, allfeats[eval(parse(text=paste('c(',fset,')',sep='')))])
if (grepl('dt', method)) mlmethod = decisiontree
if (grepl('lr', method)) mlmethod = logistic
if (grepl('ada', method)) mlmethod = adaboost
result = mlmethod(dfTrain[,feats], dfTest[,feats], target, opfunc, undersam)
if (!is.na(outpath)) write.csv(cbind(result[,c('result',target)], PID=dfTest$PID), outpath, quote=F, row.names=F)
