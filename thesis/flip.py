import ocsv
import sys

# argv[1] - input file
# argv[2] - name of the column to be flipped
# argv[3] - output file

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
maxseq = min(50, maxseq)

freq = dict()
fout = open(sys.argv[3], 'w')
fout.write(','.join(['X%d' % i for i in range(1, maxseq + 1)]) + ',Y\n')
for seq in seqs:
  # skip sequence longer than 50
  if len(seq[0]) > 50: continue
  for i in range(len(seq[0])):
    for j in range(len(seq[0]) - i):
      tmpseq = seq[0][j:j + i + 1]
      tmpseq += [''] * (maxseq - len(tmpseq))
      key = ','.join(tmpseq) + ',' + seq[1][j + i] 
      dum = fout.write(key + '\n') 
      #if key in freq:
      #  freq[key] = freq[key] + 1
      #else:
      #  freq[key] = 1

fout.close()

#posseqs = []
#for item in freq.items():
#  if item[0][-1] == '1':
#    key = item[0][0:-1] + '0'
#    if key not in freq: continue
#    count0 = freq[key]
#    prob = item[1] / (item[1] + count0)
#    if prob > 0.5: posseqs.append(item[0])
