library(rpart) #decisiontree
library(adabag) #adaboost
library(e1071) #naivebayes
library(nnet) #multinom and ann
library(class) #knn

ann <- function(df, target, size)
{
  y = class.ind(df[,target])
  train = function(data, target) nnet(data[,!names(data) %in% target], class.ind(data[,target]), size=size, softmax=T, maxit=200)
  pdt = function(model, data) predict(model, data[,!names(data) %in% target], type='class')
  kxvalid(5, df, target, train, pdt)
}

Knn <- function(df, target, k)
{
  train = function(data, target) data
  pdt = function(model, data) knn(train = model[,!names(model) %in% target], test = data[,!names(data) %in% target], cl = model[,target], k = k) 
  kxvalid(5, df, target, train, pdt)
}

multinomlogistic <- function(df, target)
{
  message('multinomlogistic ', target)
  train = function(data, target) multinom(as.formula(paste(target,'~.',sep='')), data)
  kxvalid(5, df, target, train, predict)
}

logistic <- function(df, target)
{
  message('logistic ', target)
  train = function(data, target) glm(as.formula(paste(target,'~.',sep='')), data, family=binomial())
  pdt = function(model, data) round(predict(model, data, type='response'))
  kxvalid(5, df, target, train, pdt)
}

naivebayes <- function(df, target)
{
  message('naivebayes ', target)
  train = function(data, target) naiveBayes(as.formula(paste(target,'~.',sep='')), data)
  kxvalid(5, df, target, train, predict)
}

adaboost <- function(df, target)
{
  message('adaboost ', target)
  message('acc ', 1 - boosting.cv(as.formula(paste(target,'~.',sep='')), df, v = 5)$error)
}

decisiontree <- function(df, target)
{
  message('decisiontree ', target)
  train = function(data, target) rpart(as.formula(paste(target,'~.',sep='')), data, method='class')
  pdt = function(model, data) predict(model, data, type='class')
  kxvalid(5, df, target, train, pdt)
}

# train(data, target)
# pdt(model, data)
kxvalid <- function(k, df, target, train, pdt)
{
  correct = 0
  for (i in 1:k)
  {
    b = (i - 1) * nrow(df) / k
    e = i * nrow(df) / k
    result = pdt(train(df[-(b:e),], target), df[b:e,]) 
    correct = correct + table(result == df[b:e, target])['TRUE']
    message(k, '-fold ', i, ' round acc ', correct / (i * nrow(df) / k))
  }
  message('acc ', correct / nrow(df))
  message('majority ', max(table(df[,target]))/nrow(df))
}
