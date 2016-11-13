import ocsv
import sys

# argv[1] - input file
# argv[2] - name of the column to be flipped
# argv[3] - response column
# argv[4] - output file
# argv[5] - max seq length (default 20)
inpath = sys.argv[1]
flipcol = sys.argv[2]
targetcol = sys.argv[3]
outpath = sys.argv[4]
usermax = sys.argv[5] if len(sys.argv) > 5 else None

fin = open(inpath, 'r')

col = ocsv.getColumns(fin.readline())
flipCol = col[flipcol.strip()]

currentpid = ''
maxseq = 1
# list of list of list
seqs = []
def func(line):
  global currentpid, seqs, maxseq
  row = line.strip().split(',')
  pid = row[col['PID']]
  if pid == currentpid:
    seqs[-1][0].append(row[flipCol])
    seqs[-1][1].append(row[col[targetcol]])
    maxseq = max(maxseq, len(seqs[-1][0]))
  else:
    seqs.append(([row[flipCol]], [row[col[targetcol]]]))
  currentpid = pid

if targetcol == flipcol:
    seqs = [(pair[0][:-1], pair[1][1:]) for pair in seqs if len(seqs[0]) > 1]]
    maxseq -= 1

ocsv.runFunc(fin, func)
fin.close()
maxlen = int(usermax) if usermax is not None else 20
maxlen = min(maxlen, maxseq)

freq = dict()
fouts = []
for i in range(1, maxlen + 1):
  fout = open('%s-%d.%s' % (outpath[:-4], i, outpath[-3:]), 'w')
  fout.write(','.join(['X%d' % j for j in range(1, i + 1)]) + ',Y\n')
  fouts.append(fout)
for seq in seqs:
  # skip sequence longer than maxlen
  if len(seq[0]) > maxlen: continue
  for i in range(len(seq[0])):
    for j in range(len(seq[0]) - i):
      tmpseq = seq[0][j:j + i + 1]
      #tmpseq += [''] * (maxlen - len(tmpseq))
      key = ','.join(tmpseq) + ',' + seq[1][j + i] 
      dum = fouts[len(tmpseq)-1].write(key + '\n') 

[fout.close() for fout in fouts]

