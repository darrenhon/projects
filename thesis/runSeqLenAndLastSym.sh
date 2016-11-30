#!/bin/sh
for i in `seq 1 5`
do
  screen -h 1000 -S s$i -dm bash -c "Rscript runModels.r OSHPD_CHF_BCB_KNOWNCOST_BUCKET_NOLAST_PRIVATE_VALIDATION.csv models.rds lrmodel.rds '5,33,35,12' NA $i NA '0.249930955425901 ,0.250179224766556 ,0.249710595040986 ,0.250179224766556'; exec sh"
done
#for i in `seq 1 10`
#do
#  screen -h 1000 -S l$i -dm bash -c "Rscript runModels.r OSHPD_CHF_BCB_KNOWNCOST_BUCKET_NOLAST_PRIVATE_VALIDATION.csv models.rds lrmodel.rds '5,33,35,12' NA NA $i '0.249930955425901 ,0.250179224766556 ,0.249710595040986 ,0.250179224766556'; exec sh"
#done
