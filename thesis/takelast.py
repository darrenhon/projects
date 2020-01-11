import ocsv
import sys

# argv[1] input file
# argv[2] output file

fout = open(sys.argv[2], 'w')
fin = open(sys.argv[1], 'r')
line = fin.readline()
col = ocsv.getColumns(line)
fout.write(line)

lastline = ''
def func(line):
  global lastline
  if line == None:
    fout.write(lastline)
    return
  pid = line.strip().split(',')[col['PID']]
  lastPid = pid if lastline == '' else lastline.strip().split(',')[col['PID']]
  if lastPid != pid:
    fout.write(lastline)
  lastline = line

ocsv.runFunc(fin, func, True)
fin.close()
fout.close()
