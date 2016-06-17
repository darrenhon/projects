library(rpart) #decisiontree
library(adabag) #adaboost
library(pROC) #auc

#library(e1071) #naivebayes and svm
#Svm <- function(df, target, bin=F)
#{
#  message('svm ', target)
#  train = function(data, target) svm(as.formula(paste(target,'~.',sep='')), data, family=binomial())
#  pdt = function(model, data) predict(model, data[,!names(data) %in% target])
#  kxvalid(5, df, target, train, pdt, T)
#}
#
#library(nnet) #multinom and ann
#ann <- function(df, target, size, bin=F)
#{
#  message('ann ', target)
#  y = class.ind(df[,target])
#  train = function(data, target) nnet(data[,!names(data) %in% target], class.ind(data[,target]), size=size, softmax=T, maxit=200)
#  pdt = function(model, data) predict(model, data[,!names(data) %in% target], type='class')
#  kxvalid(5, df, target, train, pdt, bin)
#}
#
#library(class) #knn
#Knn <- function(df, target, k, bin=F)
#{
#  message('knn ', target)
#  train = function(data, target) data
#  pdt = function(model, data) knn(train = model[,!names(model) %in% target], test = data[,!names(data) %in% target], cl = model[,target], k = k) 
#  kxvalid(5, df, target, train, pdt, bin)
#}
#
#multinomlogistic <- function(df, target)
#{
#  message('multinomlogistic ', target)
#  train = function(data, target) multinom(as.formula(paste(target,'~.',sep='')), data)
#  kxvalid(5, df, target, train, predict, F)
#}
#
#naivebayes <- function(df, target, bin=F)
#{
#  message('naivebayes ', target)
#  train = function(data, target) naiveBayes(as.formula(paste(target,'~.',sep='')), data)
#  kxvalid(5, df, target, train, predict, bin)
#}

logistic <- function(df, target, undersam = F)
{
  message('logistic ', target)
  train = function(data, target) glm(as.formula(paste(target,'~.',sep='')), data, family=binomial())
  pdt = function(model, data) predict(model, data, type='response')
  kxvalid(5, df, target, train, pdt, undersam)
}

adaboost <- function(df, target, initialcp = 0.01, undersam = F)
{
  message('adaboost ', target)

  formula = as.formula(paste(target,'~.',sep=''))

  # find a cp that gives non-trivial leaves
  cp = initialcp
  while (cp >= 0.0001)
  {
    message('cp ', cp)
    tree = rpart(formula, df, method='class', control=rpart.control(cp = cp, maxdepth=10))
    if (nrow(tree$frame) == 1) cp = cp / 2 else break
  }
  # if cp is too small, don't do boostrapping
  boos = cp >= 0.0001

  train = function(data, target) boosting(formula, data, boos=F, mfinal=5, control = rpart.control(cp = cp, maxdepth=10))
  pdt = function(model, data) predict.boosting(model, data)$prob[,2]
  kxvalid(5, df, target, train, pdt, undersam)
}

decisiontree <- function(df, target, initialcp = 0.01, undersam = F)
{
  message('decisiontree ', target)

  formula = as.formula(paste(target,'~.',sep=''))

  # find a cp that gives non-trivial leaves
  cp = initialcp
  while (cp >= 0.0001)
  {
    message('cp ', cp)
    tree = rpart(formula, df, method='class', control=rpart.control(cp = cp, maxdepth=10))
    if (nrow(tree$frame) == 1) cp = cp / 2 else break
  }

  train = function(data, target) 
  {
    tree = rpart(formula, data, method='class', control=rpart.control(cp = cp, maxdepth=10))
    return(prune(tree, cp=tree$cptable[which.min(tree$cptable[,"xerror"]),"CP"]))
  }
  pdt = function(model, data) predict(model, data)[,'1']
  kxvalid(5, df, target, train, pdt, undersam)
}

# train(data, target) returns model
# pdt(model, data) returns probabilities
kxvalid <- function(k, df, target, train, pdt, undersam)
{
  tauc = 0
  tpre = 0
  tsen = 0
  for (i in 1:k)
  {
    b = ((i - 1) * nrow(df) / k) + 1
    e = i * nrow(df) / k
    dftrain = df[-(b:e),]
    if (undersam) dftrain = undersampling(dftrain, target)
    prob = pdt(train(dftrain, target), df[b:e,]) 
    ans = df[b:e,target]
    oppre = optimize(pre, c(0,1), response=ans, prob=prob, maximum=T)
    thisauc = auc(ans, prob)
    tauc = tauc + thisauc
    tpre = tpre + oppre$objective
    thissen = sen(oppre$maximum, ans, prob)
    tsen = tsen + thissen
    message(k, '-fold ', i, ' round auc ', thisauc, ' pre ', oppre$objective, ' sen ', thissen)
  }
  message('auc ', tauc / k, ' pre ', tpre / k, ' sen ', tsen / k)
  message('majority ', max(table(df[,target]))/nrow(df))
}

undersampling <- function(data, target)
{
  tb = table(data[,target])
  minorlab = names(tb[tb == min(tb)])[1]
  for (lab in levels(data[,target]))
  {
    if (lab != minorlab)
    {
      removes = sample(which(data[,target] == lab), tb[lab] - tb[minorlab])
      data = data[-removes,]
    }
  }
  return(data)
}

acc <- function(thres, response, prob)
{
  lvl = levels(response)
  tb = table(response == classify(thres, lvl, prob))
  return(tb['TRUE'] / sum(tb))
}

classify <- function(thres, lvl, prob)
{
  truth = prob >= thres
  trues = which(truth)
  if (length(trues) == 0)
  {
    truth[] = lvl[1]
    return(truth)
  }
  truth[trues] = lvl[2]
  truth[-trues] = lvl[1]
  return(truth)
}

pre <- function(thres, response, prob)
{
  res = classify(thres, levels(response), prob)
  conf = table(res, response)
  if (!'1' %in% rownames(conf)) return(0) #should be 0/0
  if (!'1' %in% colnames(conf)) return(0)
  return(conf['1','1'] / sum(conf['1',]))
}

sen <- function(thres, response, prob)
{
  res = classify(thres, levels(response), prob)
  conf = table(res, response)
  if (!'1' %in% colnames(conf)) return(0) #should be 0/0
  if (!'1' %in% rownames(conf)) return(0)
  return(conf['1','1'] / sum(conf[,'1']))
}
