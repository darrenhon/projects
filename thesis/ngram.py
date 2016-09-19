import ocsv, sys, random
import sklearn.metrics

trainpath = sys.argv[1] if len(sys.argv) > 1 else ''
testpath = sys.argv[2] if len(sys.argv) > 2 else ''
n = int(sys.argv[3]) if len(sys.argv) > 3 else 4

def readData(path):
  syms = 0
  rows = []
  with open(path, 'r') as fin:
    lines, syms = [int(item) for item in fin.readline().strip().split(' ')]
    ocsv.runFunc(fin, lambda line:rows.append([int(item) for item in line.strip().split(' ')[1:]]))
  return lines, syms, rows

def train(rows):
  global ngrams
  ngrams = [[]]
  for gram in range(1, n + 1): # n gram
    igram = dict()
    for row in rows:
      #row = [-1] + row# + [-1]
      for j in range(len(row) - gram + 1):
        key = tuple(row[j:j+gram])
        igram[key] = igram[key] + 1 if key in igram else 1
    ngrams.append(igram)
  return ngrams

def predict(prefix, ngrams, syms):
  #allsyms = [-1] + list(range(syms))
  allsyms = list(range(syms))
  score = {sym:dict() for sym in allsyms}
  #prefix = tuple([-1] + prefix)
  prefix = tuple(prefix)
  grams = range(min(len(prefix) + 1, n), min(len(prefix) + 2, n + 1))
  for gram in grams:
    total = sum([ngrams[gram][tp] for tp in ngrams[gram] if tp[:gram-1] == prefix[1-gram:]])
    for sym in allsyms:
      gramseq = prefix[1 - gram:] + (sym,)
      if gramseq in ngrams[gram]:
        # score each gram by the probability
        score[sym][gram] = ngrams[gram][gramseq] / float(total)
  for sym in allsyms:
    scoredict = score[sym]
    totalgram = sum(scoredict.keys())
    # probability weighted by gram
    scoresym = sum([item[0]*item[1]/float(totalgram) for item in scoredict.items()])
    score[sym] = scoresym
  return [item for item in sorted(list(score.items()), reverse = True, key = lambda pair: pair[1])]

def test(syms, rows):
  rows = [row for row in rows if len(row) > 0]
  lines = len(rows)
  lines, syms, testrows = readData(testpath)
  ngrams = train(rows)
  probs = []
  ans = []
  pdt = []
  for row in testrows:
    for i in range(len(row) - 1):
      testrow = row[i:]
      results = predict(testrow[:-1], ngrams, syms)
      probs.append(results[0 if results[0][0] == 1 else 1][1])
      pdt.append(results[0][0])
      ans.append(testrow[-1])
  print('accuracy ', sum([1 for i in range(len(ans)) if pdt[i] == ans[i]]) / float(len(ans)))
  print('auc ', sklearn.metrics.roc_auc_score(ans, probs))

def kxvalid(k, syms, rows):
  rows = [row for row in rows if len(row) > 0]
  lines = len(rows)
  rand = random.sample(range(lines), lines)
  # k-fold x-valid
  correct = 0
  for i in range(k):
    testRange = rand[int(i * lines / k): int((i + 1) * lines / k)]
    trainRange = set(range(lines)) - set(testRange)
    ngrams = train([rows[j] for j in trainRange])
    for j in testRange:
      results = predict(rows[j][:-1], ngrams, syms)
      correct += (1 if results[0] == rows[j][-1] else 0)
    print(i, correct)
  print('accuracy ', correct / float(lines))  

def spice(syms, rows):
  import uuid
  submission = str(uuid.uuid1()).split('-')[0]
  problem = [i for i in range(6) if str(i) in testpath][0]
  ngrams = train(rows)
  with open(testpath, 'r') as fin:
    prefix = [int(i) for i in fin.readline().strip().split(' ')]
  prefixnum = 1
  while True:
    ranking = predict(prefix[1:], ngrams, syms)[:5]
    content = submit(prefix, prefixnum, ranking, problem, submission)
    print(prefixnum, ','.join([str(i) for i in ranking]), content)
    contents = content.split()
    if contents[0] == '[Error]' or contents[0] == '[Success]':
      break
    else:
      prefixnum += 1
      prefix = [int(i) for i in contents]

def main():
  lines, syms, rows = readData(trainpath)
  #spice(syms, rows)
  #kxvalid(10, syms, rows)
  test(syms, rows)

def submit(prefix, prefixnum, ranking, problem, submission):
  import urllib.parse, urllib.request
  url = 'http://spice.lif.univ-mrs.fr/submit.php?'
  params = {'user':'48',
      'problem': problem,
      'submission': submission,
      'prefix': ' '.join([str(i) for i in prefix]),
      'prefix_number': prefixnum,
      'ranking': ' '.join([str(i) for i in ranking])}
  url += urllib.parse.urlencode(params).replace('+', '%20')
  return urllib.request.urlopen(url).read().decode('utf-8').strip()

if __name__ == "__main__":
    main()
