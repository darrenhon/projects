#!/bin/sh
for i in `seq 1 35`
do
  Rscript runModels.r OSHPD_CHF_BCB_KNOWNCOST_BUCKET_NOLAST_PRIVATE.csv models.rds $i
done
