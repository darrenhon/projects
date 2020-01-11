import ocsv, sys

path = sys.argv[1]
feat = sys.argv[2]
resp = sys.argv[3]
outp = sys.argv[4]

fin = open(path, 'r')
col = ocsv.getColumns(fin.readline())
fout = open(outp, 'w')

lastRln = ''
lastResp = ''
def func(line):
  global lastRln, lastResp
  if line == None and feat != resp:
    fout.write(',' + lastResp)
    return
  row = line.strip().split(',')
  rln = row[col['PID']]
  if rln == lastRln:
    fout.write(',')
  else:
    fout.write('' if feat == resp else '' if lastRln == '' else ',' + lastResp + '\n')
  fout.write(row[col[feat]])
  lastRln = rln
  lastResp = row[col[resp]]

ocsv.runFunc(fin, func, True)
fin.close()
fout.close()
