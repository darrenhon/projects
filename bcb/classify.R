library(rpart) #decisiontree
library(adabag) #adaboost
library(e1071) #naivebayes and svm
library(nnet) #multinom and ann
library(class) #knn

Svm <- function(df, target, bin=F)
{
  message('svm ', target)
  train = function(data, target) svm(as.formula(paste(target,'~.',sep='')), data, family=binomial())
  pdt = function(model, data) predict(model, data[,!names(data) %in% target])
  kxvalid(5, df, target, train, pdt, T)
}

ann <- function(df, target, size, bin=F)
{
  message('ann ', target)
  y = class.ind(df[,target])
  train = function(data, target) nnet(data[,!names(data) %in% target], class.ind(data[,target]), size=size, softmax=T, maxit=200)
  pdt = function(model, data) predict(model, data[,!names(data) %in% target], type='class')
  kxvalid(5, df, target, train, pdt, bin)
}

Knn <- function(df, target, k, bin=F)
{
  message('knn ', target)
  train = function(data, target) data
  pdt = function(model, data) knn(train = model[,!names(model) %in% target], test = data[,!names(data) %in% target], cl = model[,target], k = k) 
  kxvalid(5, df, target, train, pdt, bin)
}

multinomlogistic <- function(df, target)
{
  message('multinomlogistic ', target)
  train = function(data, target) multinom(as.formula(paste(target,'~.',sep='')), data)
  kxvalid(5, df, target, train, predict, F)
}

logistic <- function(df, target)
{
  message('logistic ', target)
  train = function(data, target) glm(as.formula(paste(target,'~.',sep='')), data, family=binomial())
  pdt = function(model, data) round(predict(model, data, type='response'))
  kxvalid(5, df, target, train, pdt, T)
}

naivebayes <- function(df, target, bin=F)
{
  message('naivebayes ', target)
  train = function(data, target) naiveBayes(as.formula(paste(target,'~.',sep='')), data)
  kxvalid(5, df, target, train, predict, bin)
}

adaboost <- function(df, target, bin=F)
{
  message('adaboost ', target)
  message('acc ', 1 - boosting.cv(as.formula(paste(target,'~.',sep='')), df, v = 5)$error)
}

decisiontree <- function(df, target, bin=F)
{
  message('decisiontree ', target)
  train = function(data, target) rpart(as.formula(paste(target,'~.',sep='')), data, method='class')
  pdt = function(model, data) predict(model, data, type='class')
  kxvalid(5, df, target, train, pdt, bin)
}

# train(data, target)
# pdt(model, data)
kxvalid <- function(k, df, target, train, pdt, bin)
{
  tt = 0
  ttp = NA
  ttn = NA
  tfp = NA
  tfn = NA
  for (i in 1:k)
  {
    b = ((i - 1) * nrow(df) / k) + 1
    e = i * nrow(df) / k
    res = pdt(train(df[-(b:e),], target), df[b:e,]) 
    ans = df[b:e,target]
    t = sum(res == ans)
    message(k, '-fold ', i, ' round acc ', t / (floor(e) - floor(b) + 1))
    tt = tt + t
    if (bin)
    {
      tp = sum(res == ans & res == 1)
      tn = sum(res == ans & res == 0)
      fp = sum(res != ans & res == 1)
      fn = sum(res != ans & res == 0)
      ttp = if (is.na(ttp)) tp else ttp + tp
      ttn = if (is.na(ttn)) tn else ttn + tn
      tfp = if (is.na(tfp)) fp else tfp + fp
      tfn = if (is.na(tfn)) fn else tfn + fn
      message(k, '-fold ', i, ' round pre ', tp / (tp + fp), ' sen ', tp / (tp + fn), ' spe ', tn/(tn+fp))
    }
  }
  message('acc ', tt / nrow(df))
  message('majority ', max(table(df[,target]))/nrow(df))
  if (bin) message('pre ', ttp / (ttp + tfp), ' sen ', ttp / (ttp + tfn), ' spe ', ttn/(ttn+tfp))
}
