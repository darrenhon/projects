library(e1071)
library(party)
source('likePredict.r')
igage = read.csv('ig_age.csv', check.names=F)
iggender = read.csv('ig_gender.csv', check.names=F)
ft = read.csv('flatten.csv', check.names=F)
tops = scan('top10000.csv')

train = ft
setTarget = function(target)
{
  # remove non-target attributes
  train <<- ft
  for (i in 9:1)
  {
    if (colnames(ft)[i] != target) train <<- train[,-i] 
  }
  train$age <<- convertAge(train$age)
}

setColRange = function(range)
{
  # select the top information gain
  cols = c(igage[range, 1], '31159065182', '20060875764', '167641889920409', '9588466619', '10643211755')
  #cols = iggender[range, 1]
  #cols = tops[range]

  # extract features
  train <<- train[,c(1, which(names(train) %in% cols))]

  # convert to factor for classification
  for (i in 1:ncol(train)) train[,i] <<- as.factor(train[,i])
}

models = c()
testSample = function(sampleSize)
{
  models <<- c()
  # train tree
  # random test sample
  results = c()
  for (crossv in 1:10)
  {
    # random xvalidation
    testrange = sample.int(nrow(train), sampleSize)
    # deterministic xvalidation
    #testrange = (((crossv - 1) * 950) + 1) : (crossv * 950)
    model = ctree(as.formula(paste(colnames(train)[1], '~ .')), train[-testrange,])
    models <<- c(models, model)

    # classification
    pdt = predict(model, train[testrange,-1]) == train[testrange,1]
    result = length(pdt[pdt==TRUE])/length(testrange)
    baseline = nrow(train[testrange,][train[testrange,]$age=='xx-24',])/length(testrange)
    print(sprintf('acc %.4f baseline %.4f gain %.4f', result, baseline, result-baseline))

    # regression
    #pdt = predict(model, train[testrange,-1]) - train[testrange,1]
    #result = sqrt(mean(pdt^2))
    #baseline = sqrt(mean((train[testrange,1] - mean(train[,1]))^2))
    #print(sprintf('rmse %.4f baseline %.4f gain %.4f', result, baseline, baseline-result))

    results = c(results, result)
  }
  print(mean(results))
}

fulltest = function(model)
{
  testrange = 1:9500
  pdt = predict(model, train[testrange,-1]) == train[testrange,1]
  result = length(pdt[pdt==TRUE])/length(testrange)
  baseline = nrow(train[testrange,][train[testrange,]$age=='xx-24',])/length(testrange)
  print(sprintf('acc %.4f baseline %.4f gain %.4f', result, baseline, result-baseline))
}

setTarget('age')
setColRange(1:100)
testSample(1000)

