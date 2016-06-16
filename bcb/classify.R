library(rpart) #decisiontree
library(adabag) #adaboost
library(stats) #optim
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

logistic <- function(df, target)
{
  message('logistic ', target)
  train = function(data, target) glm(as.formula(paste(target,'~.',sep='')), data, family=binomial())
  pdt = function(model, data) predict(model, data, type='response')
  kxvalid(5, df, target, train, pdt)
}

adaboost <- function(df, target, initialcp = 0.01)
{
  message('adaboost ', target)

  formula = as.formula(paste(target,'~.',sep=''))

  # find a cp that gives non-trivial leaves
  cp = initialcp
  while (T)
  {
    message('cp ', cp)
    tree = rpart(formula, df, control=rpart.control(cp = cp, maxdepth=10))
    if (nrow(tree$frame) == 1) cp = cp / 2 else break
  }

  train = function(data, target) boosting(formula, data, boos=F, mfinal=5, control = rpart.control(cp = cp, maxdepth=10))
  pdt = function(model, data) predict.boosting(model, data)$prob[,2]
  kxvalid(5, df, target, train, pdt)
}

decisiontree <- function(df, target, initialcp = 0.01)
{
  message('decisiontree ', target)

  formula = as.formula(paste(target,'~.',sep=''))

  # find a cp that gives non-trivial leaves
  cp = initialcp
  while (T)
  {
    message('cp ', cp)
    tree = rpart(formula, df, control=rpart.control(cp = cp, maxdepth=10))
    if (nrow(tree$frame) == 1) cp = cp / 2 else break
  }

  train = function(data, target) 
  {
    tree = rpart(formula, data, method='class', control=rpart.control(cp = cp, maxdepth=10))
    return(prune(tree, cp=tree$cptable[which.min(tree$cptable[,"xerror"]),"CP"]))
  }
  pdt = function(model, data) predict(model, data)[,'1']
  kxvalid(5, df, target, train, pdt)
}

# train(data, target)
# pdt(model, data)
kxvalid <- function(k, df, target, train, pdt)
{
  tauc = 0
  tt = 0
  ttp = NA
  ttn = NA
  tfp = NA
  tfn = NA
  for (i in 1:k)
  {
    b = ((i - 1) * nrow(df) / k) + 1
    e = i * nrow(df) / k
    prob = pdt(train(df[-(b:e),], target), df[b:e,]) 
    ans = df[b:e,target]
    op = optimize(acc, c(0,1), response=ans, prob=prob, maximum=T)
    res = classify(op$maximum, levels(ans), prob)
    t = sum(res == ans)
    thisauc = auc(roc(ans, prob))
    message(k, '-fold ', i, ' round acc ', t / (floor(e) - floor(b) + 1), ' auc ', thisauc)
    tauc = tauc + thisauc
    tt = tt + t
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
  message('acc ', tt / nrow(df))
  message('auc ', tauc / k)
  message('majority ', max(table(df[,target]))/nrow(df))
  message('pre ', ttp / (ttp + tfp), ' sen ', ttp / (ttp + tfn), ' spe ', ttn/(ttn+tfp))
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
