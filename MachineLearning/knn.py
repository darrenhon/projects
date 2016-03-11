import random
import sys
from math import pow, sqrt, log
from statistics import pstdev, mean

pers = dict([(2,'age'),(4,'ope'),(5,'con'),(6,'ext'),(7,'agr'),(8,'neu')])

# private function
# construct maps of users to likes
def getFilteredUserLikes(path, minlikes, maxlikes):
  frel = open(path + '/relation/relation.csv', 'r')
  likes = dict()
  users = dict()
  line = frel.readline()
  for line in frel:
    row = line.strip().split(',')
    if row[2] in likes:
      likes[row[2]] = likes[row[2]] + 1
    else:
      likes[row[2]] = 1
    if row[1] in users:
      users[row[1]].add(row[2])
    else:
      users[row[1]] = set([row[2]])
  frel.close()
  sortedLikes = sorted(list(likes.items()), key = lambda item: item[1], reverse=True)
  filtered = dict()
  filteredNames = []
  i = 0
  for item in sortedLikes:
    if item[1] >= minlikes and item[1] <= maxlikes:
      filteredNames.append(item[0])
      filtered[item[0]] = i
      i = i + 1
  sfiltered = set(filteredNames)
  for user in users:
    users[user] = users[user] & sfiltered
  return (users, filtered)

# convert age into 1-4
def bucketAge(age):
  if float(age) < 25:
    return '4'
  elif float(age) < 35:
    return '3'
  elif float(age) < 50:
    return '2'
  return '1'

# private function to get user profiles
def getProfiles():
  result = []
  fpro = open(trainPath + '/profile/profile.csv', 'r')
  line = fpro.readline()
  for line in fpro:
    row = line.strip().split(',')
    if row[2] != '-':
      row[2] = bucketAge(row[2])
    result.append(row)
  fpro.close()
  return result

# this function must be called before everything else to set the testing data path and training data path
# have to be the same path for local test data
def initialize(inputpath, trainpath):
  global inputPath
  inputPath = inputpath
  global trainPath
  trainPath = trainpath
  global trainUsers, trainPages
  trainUsers, trainPages = getFilteredUserLikes(trainPath, 0, 2000)
  global testUsers
  if inputPath == trainPath:
    testUsers = trainUsers
  else:
    testUsers = getFilteredUserLikes(inputPath, 0, 2000)[0]
  global pro
  pro = getProfiles()
  global stats
  stats = None

# run knn for single instance
# col is column number. for example age is 2, gender is 3, ope is 4, etc.
# classify is a boolean. True for classification and False for regression
# weighted is a boolean. True for using jaccard similarities as weight. False for equal weights
# default is the value when the user has no neighbor. Usually the baseline value.
def knnSingle(userid, k, col, classify, weighted, default):
  js = []
  ilikes = testUsers[userid]
  for j in range(len(pro)):
    jlikes = trainUsers[pro[j][1]]
    sim = len(ilikes&jlikes)
    if sim == 0: continue
    js.append((j, float(sim)/len(ilikes|jlikes)))
  return predictknn(col, js, k, classify, weighted, default) if len(js) > 0 else default

# run knn on a number of random sample
# col is column number. for example age is 2, gender is 3, ope is 4, etc.
# classify is a boolean. True for classification and False for regression
# weighted is a boolean. True for using jaccard similarities as weight. False for equal weights
# default is the value when the user has no neighbor. Usually the baseline value.
# bias is a numeric value for regression only. 
def knnAll(k, sample, col, classify, weighted, default, bias):
  testrange = random.sample(range(0, len(pro)), sample)
  correct = 0
  se = 0
  count = 0
  errs = []
  for i in testrange:
    count = count + 1
    if count % (len(testrange) / 10.0) == 0:
      print(count * 100.0 / len(testrange))
    #jacard similarities [user index, simularity]
    js = []
    ilikes = testUsers[pro[i][1]]
    for j in range(len(pro)):
      if i == j: continue
      jlikes = trainUsers[pro[j][1]]
      sim = len(ilikes&jlikes)
      if sim == 0: continue
      js.append((j, float(sim)/len(ilikes|jlikes)))
    predict = predictknn(col, js, k, classify, weighted, default) if len(js) > 0 else default
    if classify:
      if predict == pro[i][col]: correct = correct + 1
    else:
      predict = predict + bias
      err = predict - float(pro[i][col])
      errs.append(err)
      se = se + pow(err, 2)
  if classify:
    print('acc', float(correct)/len(testrange))
  else:
    print('rmse', sqrt(float(se)/len(testrange)))
  return errs

# private function
# run knn given jacard similarities
def predictknn(col, js, k, classify, weighted, default):
    sortjs = sorted(js, key = lambda item:item[1], reverse=True)
    if len(sortjs) > k:
      sortjs = [item for item in sortjs if item[1] >= sortjs[k - 1][1]]
    if not weighted:
      for i in range(len(sortjs)): sortjs[i] = (sortjs[i][0], 1)
    if classify:
      labelsim = dict()
      for item in sortjs:
        label = pro[item[0]][col]
        if label in labelsim:
          labelsim[label] = labelsim[label] + item[1]
        else:
          labelsim[label] = item[1]
      maxsim = [item[0] for item in list(labelsim.items()) if item[1] == max(labelsim.values())]
      if len(maxsim) > 1: 
        #print('Draw')
        return max(maxsim)
      return maxsim[0]
    else:
      return sum([float(pro[item[0]][col]) * item[1] for item in sortjs]) / sum([item[1] for item in sortjs])


# flattening (not used anymore but keep it here)
def flatten(users, flattened):
  fout = open('flatten.csv', 'w')
  fpro = open('..\profile\profile.csv', 'r')
  line = fpro.readline().strip()
  fout.write(line + ','.join(filteredNames) + '\n')
  count = 0
  for line in fpro:
    line = line.strip()
    row = line.split(',')
    flattenlikes = [0] * len(filtered)
    count = count + 1
    if count % 500 == 0: print(count)
    for likeid in users[row[1]]:
      flattenlikes[filtered[likeid]] = 1
    dum = fout.write(line + ','.join([str(item) for item in flattenlikes]) + '\n')
  fout.close()
  fpro.close()

# proprietary model
# test over a range
# testrange is a range object. for example range(0,1000) to test the first 1000 data.
# col is column number. for example age is 2, gender is 3, ope is 4, etc.
# default is the value when the user has no neighbor. Usually the baseline value.
# bias is the value added to the predicted value
# minlike is the minimum number of users who like the pages
# maxlike is the maximum number of users who like the pages
def weightedAverageRange(testrange, col, default, bias = 0, minlike = 0, maxlike = 2000):
  se = 0
  errs = []
  defcount = 0
  for i in testrange:
    predict = weightedAverage(pro[i][1], col, default, bias, minlike, maxlike)
    if (predict == default): defcount = defcount + 1
    err = predict - float(pro[i][col])
    errs.append(err)
    se = se + pow(err, 2)
  rmse = sqrt(float(se)/len(testrange))
  return (rmse, mean(errs), defcount)

# predict single instance
def weightedAverage(userid, col, default, bias, minlike = 0, maxlike = 2000):
  likes = testUsers[userid]
  pairs = []
  for like in likes:
    if (like, pers[col]) not in stats: continue
    stat = stats[(like, pers[col])]
    if stat[0] < minlike or stat[0] > maxlike: continue
    if stat[0] == 1: weight = 1
    else: weight = 1 / (0.00001 + stat[2])
    pairs.append((stat[1], weight))
  if len(pairs) == 0: return default
  return sum([pair[0]*pair[1] for pair in pairs])/sum([pair[1] for pair in pairs]) + bias

# load the csv into stats
def loadStats():
  global stats
  if stats != None: return
  stats = dict()
  fin = open('likestats.csv', 'r')
  for line in fin:
    row = line.strip().split(',')
    stats[(row[0],row[1])] = [float(row[2]), float(row[3]), float(row[4])]
  fin.close()

# proprietary model need stats to run
# run this function for the first time and stats will be saved as a csv
# trainrange is the range to generate the stats. For example range(0,8500) to generate stats for top 8500 data
# save is a boolean whether to save the csv file
def buildStats(trainrange, save):
  if save:
    fout = open('likestats.csv', 'w')
  global stats
  stats = dict()
  for i in trainrange:
    user = pro[i]
    likes = trainUsers[user[1]]
    for like in likes:
      for per in pers.items():
        pair = (like, per[1])
        value = float(user[per[0]]) 
        if pair in stats:
          stats[pair].append(value)
        else:
          stats[pair] = [value]
  for item in stats.items():
    values = item[1]
    avg = mean(values)
    stat = (len(values), avg, sqrt(sum([pow(value - avg, 2) for value in values])/len(values)))
    stats[item[0]] = stat
    if save:
      row = item[0] + stat
      fout.write(','.join([str(entry) for entry in item[0] + stat]) + '\n')
  if save:
    fout.close()
  
# test over all data
def weightedAverageAll():
  testrange = random.sample(range(0, 9500), 9500)
  results = []
  for i in range(0,10):
    print (i)
    subtestrange = testrange[i * 950:(i+1) * 950]
    traindata = set(range(0,9500)) - set(subtestrange)
    buildStats(traindata, False)
    results.append((i, 2) + weightedAverageRange(subtestrange, 2, 4, 0, 9, 90))
    results.append((i, 4) + weightedAverageRange(subtestrange, 4, 3.909, -0.064, 5, 38))
    results.append((i, 5) + weightedAverageRange(subtestrange, 5, 3.446, 0.077, 5, 164))
    results.append((i, 6) + weightedAverageRange(subtestrange, 6, 3.487, -0.007, 5, 164))
    results.append((i, 7) + weightedAverageRange(subtestrange, 7, 3.584, 0.01, 25, 42))
    results.append((i, 8) + weightedAverageRange(subtestrange, 8, 2.732, -0.0729, 5, 51))
    for j in range(-5, 0, 1): print(results[j])
  return results
