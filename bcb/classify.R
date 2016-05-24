library(rpart)
library(adabag)

adaboost <- function(df, target)
{
  message('acc ', 1 - boosting.cv(as.formula(paste(target,'~.',sep='')), df)$error)
}

decisionTree <- function(df, target)
{
  train = function(data, target) rpart(as.formula(paste(target,'~.',sep='')), data, method='class')
  pdt = function(model, data) predict(model, data, type='class')
  kxvalid(10, df, target, train, pdt)
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
  }
  message('acc ', correct / nrow(df))
}
