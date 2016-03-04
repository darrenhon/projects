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
    return '4'
  elif float(age) < 35:
    return '3'
  elif float(age) < 50:
    return '2'
  return '1'

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

def knnSingle(userid, k, col, classify, weighted, default):
  js = []
  ilikes = testUsers[userid]
  for j in range(len(pro)):
    jlikes = trainUsers[pro[j][1]]
    sim = len(ilikes&jlikes)
    if sim == 0: continue
    js.append((j, float(sim)/len(ilikes|jlikes)))
  return predictknn(col, js, k, classify, weighted, default) if len(js) > 0 else default

# knn
def knnAll(k, sample, col, classify, weighted, default):
  testrange = random.sample(range(0, len(pro)), sample)
  correct = 0
  se = 0
  count = 0
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
      se = se + pow(predict - float(pro[i][col]), 2)
  if classify:
    print('acc', float(correct)/len(testrange))
  else:
    print('rmse', sqrt(float(se)/len(testrange)))

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
        print('Draw')
        return max(maxsim)
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
