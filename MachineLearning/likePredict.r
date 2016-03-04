library(e1071)
library(party)
library(rPython)

igage = read.csv('ig_age.csv', check.names=F)
iggender = read.csv('ig_gender.csv', check.names=F)
tops = scan('top10000.csv')

predictAgeByLikes <- function(userid, dfRelations, model)
{
  cols = c(igage[1:100, 1], '31159065182', '20060875764', '167641889920409', '9588466619', '10643211755')
  df = data.frame(matrix(nrow=1, ncol=length(cols)))
  colnames(df) = cols
  for (i in 1:ncol(df)) 
  {
    df[,i] = as.factor(df[,i])
    levels(df[,i]) = c('0','1')
  }
  df = rbind(df, as.numeric(cols %in% dfRelations[dfRelations$userid == userid, 'like_id']))
  df = df[-1,]
  return(predict(model, df))
}

initializeKnn <- function(input)
{
  python.load('knn.py')
  python.exec(sprintf("initialize('%s', '/data/training')", input))
}

predictGenderByLikes <- function(userid, dfRelations, model)
{
  result = python.get(sprintf("knnSingle('%s', %d, %d, True, True)", userid, 30, 3))
  if (is.null(result)) result = 1
  return(round(as.numeric(result) + 0.01))
}

#predictGenderByLikes <- function(userid, dfRelations, model)
#{
#  cols = iggender[1:100, 1]
#  df = data.frame(matrix(nrow=1, ncol=length(cols)))
#  colnames(df) = cols
#  for (i in 1:ncol(df)) 
#  {
#    df[,i] = as.factor(df[,i])
#    levels(df[,i]) = c('0','1')
#  }
#  df = rbind(df, as.numeric(cols %in% dfRelations[dfRelations$userid == userid, 'like_id']))
#  df = df[-1,]
#  return(predict(model, df))
#}

predictPersonalityByLikes <- function(userid, dfRelations, model, featRange)
{
  cols = tops[featRange]
  df = data.frame(matrix(nrow=1, ncol=length(cols)))
  colnames(df) = cols
  for (i in 1:ncol(df)) 
  {
    df[,i] = as.integer(df[,i])
  }
  df = rbind(df, as.integer(cols %in% dfRelations[dfRelations$userid == userid, 'like_id']))
  df = df[-1,]
  return(predict(model, df))
}


convertAge <-function(column)
{
  return(cut(column,
             breaks = c(-Inf, 24, 34, 49, Inf),
             labels = c("xx-24", "25-34", "35-49", "50-xx"),
             right = T))
}
