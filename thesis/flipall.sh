#!/bin/bash
for col in $(head -1 $1 | sed 's/,/ /g' | tr -d '\r\n')
do
  python3 flip.py $1 $col $2 $3/$col.csv
done
