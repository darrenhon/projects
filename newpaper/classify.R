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

logistic <- function(df, dfTest, target, opfunc, undersam)
{
  message('logistic ', target)
  train = function(data, target) glm(as.formula(paste(target,'~.',sep='')), data, family=binomial())
  pdt = function(model, data) predict(model, data, type='response')
  if (!is.na(dfTest)) {
    return(trainAndTest(df, dfTest, target, train, pdt, undersam))
  } else {
    return(kxvalid(5, df, target, train, pdt, opfunc, undersam))
  }
}

adaboost <- function(df, dfTest, target, opfunc, undersam, cp = NA)
{
  # adaboost is too slow. random sample 1/5
  #df = df[sort(sample(1:nrow(df), nrow(df) / 5)),]

  message('adaboost ', target)

  formula = as.formula(paste(target,'~.',sep=''))

  # find a cp that gives at least 20 nodes
  if (is.na(cp))
  {
    cp = 0.01
    while (cp >= 0.00001)
    {
      message('cp ', cp)
      tree = rpart(formula, df, method='class', control=rpart.control(cp = cp))
      if (nrow(tree$frame) < 20) cp = cp / 2 else break
    }
  }
  message('final cp ', cp)

  train = function(data, target) boosting(formula, data, boos=F, mfinal=20, control = rpart.control(cp = cp))
  pdt = function(model, data) predict.boosting(model, data)$prob[,2]
  if (!is.na(dfTest)) {
    return(trainAndTest(df, dfTest, target, train, pdt, undersam))
  } else {
    return(kxvalid(5, df, target, train, pdt, opfunc, undersam))
  }
}

decisiontree <- function(df, dfTest, target, opfunc, undersam, cp = NA)
{
  message('decisiontree ', target)

  formula = as.formula(paste(target,'~.',sep=''))

  # find a cp that gives at least 50 nodes
  if (is.na(cp))
  {
    cp = 0.01
    while (cp >= 0.00001)
    {
      message('cp ', cp)
      tree = rpart(formula, df, method='class', control=rpart.control(cp = cp))
      if (nrow(tree$frame) < 50) cp = cp / 2 else break
    }
  }
  message('final cp ', cp)

  train = function(data, target) 
  {
    tree = rpart(formula, data, method='class', control=rpart.control(cp = cp))
    return(prune(tree, cp=tree$cptable[which.min(tree$cptable[,"xerror"]),"CP"]))
  }
  pdt = function(model, data) predict(model, data)[,'1']
  if (!is.na(dfTest)) {
    return(trainAndTest(df, dfTest, target, train, pdt, undersam))
  } else {
    return(kxvalid(5, df, target, train, pdt, opfunc, undersam))
  }
}

# no optimizer. no sensitivity, accuracy and precision.
# train(data, target) returns model
# pdt(model, data) returns probabilities
trainAndTest <- function(dfTrain, dfTest, target, train, pdt, undersam)
{
  if (undersam) dfTrain = undersampling(dfTrain, target)
  prob = pdt(train(dfTrain, target), dfTest) 
  ans = dfTest[,target]
  thisauc = auc(ans, prob)
  message('auc ', thisauc)
  message('majority ', max(table(dfTest[,target]))/nrow(dfTest))
  return(cbind(dfTest, result=prob))
}

# train(data, target) returns model
# pdt(model, data) returns probabilities
kxvalid <- function(k, df, target, train, pdt, opfunc, undersam)
{
  tauc = 0
  tpre = 0
  tsen = 0
  pv = list()
  for (i in 1:k)
  {
    b = as.integer(((i - 1) * nrow(df) / k) + 1)
    e = as.integer(i * nrow(df) / k)
    # remove predicted LOS for training
    #dftrain = df[-(b:e), -which(names(df) == 'pnextLOS_b')] 
    dftrain = df[-(b:e),] 
    dftest = df[b:e,]
    # replace nextLOS_b for testing
    #dftest[,'nextLOS_b'] = dftest[,'pnextLOS_b'] 
    #dftest = dftest[,-which(names(df) == 'pnextLOS_b')]
    if (undersam) dftrain = undersampling(dftrain, target)
    prob = pdt(train(dftrain, target), dftest) 
    ans = dftest[,target]
    thisauc = auc(ans, prob)
    op = optimize(get(opfunc), c(0,1), response=ans, prob=prob, maximum=T)
    res = classify(op$maximum, prob)
    pv = append(pv, prob)
    conf = table(res, ans)
    thissen = sensitivity(conf)
    thispre = precision(conf)
    tauc = tauc + thisauc
    tpre = tpre + thispre
    tsen = tsen + thissen
    message(k, '-fold ', i, ' round auc ', thisauc, ' pre ', thispre, ' sen ', thissen, ' ', opfunc, ' ', op$objective)
  }
  message('auc ', tauc / k, ' pre ', tpre / k, ' sen ', tsen / k)
  message('majority ', max(table(df[,target]))/nrow(df))
  return(cbind(df, result=unlist(pv)))
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

pre <- function(thres, response, prob)
{
  res = classify(thres, prob)
  conf = table(res, response)
  return(precision(conf))
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
