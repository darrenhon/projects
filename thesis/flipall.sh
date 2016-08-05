#!/bin/bash
for col in $(head -1 $1 | sed 's/,/ /g')
do
  python3 flip.py $1 $col $2/$col.csv
done
