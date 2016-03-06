library(rPython)

initializeKnn <- function(input)
{
  python.load('knn.py')
  python.exec(sprintf("initialize('%s', '/data/training')", input))
}

predictGenderByLikes <- function(userid)
{
  return(as.numeric(python.get(sprintf("knnSingle('%s', %d, %d, True, False, '1.0')", userid, 43, 3))))
}

predictAgeByLikes <- function(userid)
{
  result = python.get(sprintf("knnSingle('%s', %d, %d, True, True, '4')", userid, 19, 2))
  if (result == '4') return('xx-24')
  if (result == '3') return('25-34')
  if (result == '2') return('35-49')
  return('50-xx')
}

convertAge <-function(column)
{
  return(cut(column,
             breaks = c(-Inf, 24, 34, 49, Inf),
             labels = c("xx-24", "25-34", "35-49", "50-xx"),
             right = T))
}
