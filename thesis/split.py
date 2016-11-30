import ocsv
import sys
import random

# argv[1] input file
# argv[2] first output file
# argv[3] second output file

f1 = open(sys.argv[2], 'w')
f2 = open(sys.argv[3], 'w')
fin = open(sys.argv[1], 'r')
line = fin.readline()
col = ocsv.getColumns(line)
f1.write(line)
f2.write(line)

pid1, pid2 = '', ''
def func(line):
  global pid1, pid2
  row = line.strip().split(',')
  pid = row[col['PID']]
  if pid != pid1 and pid != pid2:
    if int(row[col['gender']]) == 0: # 0 is female
      pid1 = pid
    else:
      pid2 = pid
  if pid == pid1:
    f1.write(line)
  else:
    f2.write(line)

ocsv.runFunc(fin, func)
fin.close()
f1.close()
f2.close()
