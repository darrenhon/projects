import random
import sys
from math import pow, sqrt

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

def bucketAge(age):
  if float(age) < 25:
    return 'xx-24'
  elif float(age) < 35:
    return '25-34'
  elif float(age) < 50:
    return '35-49'
  return '50-xx'

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

def initialize(inputpath, trainpath):
  global inputPath
  inputPath = inputpath
  global trainPath
  trainPath = trainpath
  global testUsers
  testUsers = getFilteredUserLikes(inputPath, 0, 2000)[0]
  global trainUsers
  if inputPath == trainPath:
    trainUsers = testUsers
  else:
    trainUsers = getFilteredUserLikes(trainPath, 0, 2000)[0]
  global pro
  pro = getProfiles()

def convertDummy(dum):
  return dum

def convertGender(gender):
  return float(gender)

def knnOpe(k, sample, weighted = True):
  knnAll(k, sample, 4, False, weighted, '1.0')

def knnAge(k, sample, weighted = True):
  knnAll(k, sample, 2, True, weighted, '1.0')

def knnGender(k, sample, weighted = True):
  knnAll(k, sample, 3, True, weighted, '1.0', convertGender)

def knnSingle(userid, k, col, classify, weighted, default = None):
  js = [0] * len(pro)
  ilikes = testUsers[userid]
  for j in range(0,len(pro)):
    useridj = pro[j][1]
    jlikes = trainUsers[useridj]
    if len(ilikes) == 0 or len(jlikes) == 0: continue
    sim = float(len(ilikes&jlikes))/len(ilikes|jlikes)
    if sim == 0: continue
    js[j] = (j, sim)
  js = [item for item in js if item != 0]
  predict = 1
  if (len(js) > 0):
    return predictknn(col, js, k, weighted, default)

# knn
def knnAll(k, sample, col, classify, weighted, default, conPredict = convertDummy, conActual = convertDummy):
  testrange = random.sample(range(0, len(pro)), sample)
  correct = 0
  se = 0
  count = 0
  for i in testrange:
    count = count + 1
    if count % (len(testrange) / 10.0) == 0:
      print(count * 100.0 / len(testrange))
    #jacard similarities [user index, simularity]
    js = [0] * len(pro)
    trainrange = set(range(0,len(pro)))
    trainrange.remove(i)
    ilikes = testUsers[pro[i][1]]
    for j in trainrange:
      useridj = pro[j][1]
      jlikes = trainUsers[useridj]
      if len(ilikes) == 0 or len(jlikes) == 0: continue
      sim = float(len(ilikes&jlikes))/len(ilikes|jlikes)
      if sim == 0: continue
      js[j] = (j, sim)
    js = [item for item in js if item != 0]
    predict = default
    if (len(js) > 0):
      predict = predictknn(col, js, k, classify, weighted, default)
    if classify:
      if conPredict(predict) == conActual(pro[i][col]): correct = correct + 1
    else:
      se = se + pow(predict - float(pro[i][col]), 2)
  if classify:
    print('acc', float(correct)/len(testrange))
  else:
    print('rmse', sqrt(float(se)/len(testrange)))

def predictknn(col, js, k, classify, weighted, default):
    sortjs = sorted([item for item in js], key = lambda item:item[1], reverse=True)
    if len(sortjs) > k:
      sortjs = [item for item in sortjs if item[1] >= sortjs[k - 1][1]]
    if not weighted:
      for item in sortjs: item[1] = 1
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
        print('Draw')
        return maxsim[0] if default is None else default
      return maxsim[0]
    else:
      return (sum([float(pro[item[0]][col]) * item[1] for item in sortjs]) / sum([item[1] for item in sortjs]))


# flattening
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
