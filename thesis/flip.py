import ocsv
import sys

# argv[1] - input file
# argv[2] - name of the column to be flipped
# argv[3] - output file
# argv[4] - max seq length (default 20)

fin = open(sys.argv[1], 'r')

col = ocsv.getColumns(fin.readline())
flipCol = col[sys.argv[2]]

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
    seqs[-1][1].append(row[col['thirtyday']])
    maxseq = max(maxseq, len(seqs[-1][0]))
  else:
    seqs.append([[row[flipCol]], [row[col['thirtyday']]]])
  currentpid = pid

ocsv.runFunc(fin, func)
fin.close()
maxlen = int(sys.argv[4]) if len(sys.argv) == 5 else 20
maxlen = min(maxlen, maxseq)

freq = dict()
fouts = []
outname = sys.argv[3]
for i in range(1, maxlen + 1):
  fout = open('%s-%d.%s' % (outname[:-4], i, outname[-3:]), 'w')
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

