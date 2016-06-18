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
  # adaboost is too slow. random sample 1/10
  df = df[sort(sample(1:nrow(df), nrow(df) / 10)),]

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
  message('final cp ', cp)
  boos = cp >= 0.0001

  train = function(data, target) boosting(formula, data, boos=boos, mfinal=10, control = rpart.control(cp = cp, maxdepth=10))
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
  message('final cp ', cp)

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
    thisauc = auc(ans, prob)
    op = optimize(fscore, c(0,1), response=ans, prob=prob, maximum=T)
    res = classify(op$maximum, prob)
    conf = table(res, ans)
    thissen = sensitivity(conf)
    thispre = precision(conf)
    tauc = tauc + thisauc
    tpre = tpre + thispre
    tsen = tsen + thissen
    message(k, '-fold ', i, ' round auc ', thisauc, ' pre ', thispre, ' sen ', thissen, ' f-score ', op$objective)
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
  tb = table(response == classify(thres, prob))
  return(tb['TRUE'] / sum(tb))
}

# assume binary classes '0' and '1'
classify <- function(thres, prob)
{
  truth = prob >= thres
  trues = which(truth)
  if (length(trues) == 0)
  {
    truth[] = '0'
    return(truth)
  }
  truth[trues] = '1'
  truth[-trues] = '0'
  return(truth)
}

# assume binary classes '0' and '1'
precision <- function(conf)
{
  if (!'1' %in% rownames(conf)) return(NaN) #should be 0/0
  if (!'1' %in% colnames(conf)) return(0)
  return(conf['1','1'] / sum(conf['1',]))
}

# assume binary classes '0' and '1'
sensitivity <- function(conf)
{
  if (!'1' %in% colnames(conf)) return(NaN) #should be 0/0
  if (!'1' %in% rownames(conf)) return(0)
  return(conf['1','1'] / sum(conf[,'1']))
}

fscore <- function(thres, response, prob)
{
  res = classify(thres, prob)
  conf = table(res, response)
  pre = precision(conf)
  sen = sensitivity(conf)
  return((2*pre*sen)/(pre+sen))
}
