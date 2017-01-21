import ocsv
import sys

# argv[1] input file
# argv[2] output file

fout = open(sys.argv[2], 'w')
fin = open(sys.argv[1], 'r')
line = fin.readline()
col = ocsv.getColumns(line)
fout.write(line)

def func(line):
  row = line.strip().split(',')
  row[col['nextCost_b']] = '1' if int(row[col['nextCost_b']]) > 3 else '0'
  row[col['nextLOS_b']] = '1' if int(row[col['nextLOS_b']]) > 4 else '0'
  fout.write(','.join(row) + '\n')

ocsv.runFunc(fin, func)
fin.close()
fout.close()
