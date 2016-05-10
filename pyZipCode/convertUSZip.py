fin = open('US.txt','r')
fout = open('newpyZipCode.py','w')
for line in fin:
  row = [item.strip() for item in line.strip('\n').split('\t')]
  dum = fout.write("'" + row[1] + "':(" + row[-3] + ',' + row[-2] + '),\n')

fin.close()
fout.close()
