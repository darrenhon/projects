#!/bin/sh
for i in `seq 1 35`
do
  Rscript runModels.r $1 $2 $3 $4 $i
done
