source('classify.R')
library(data.table)

df=fread(commandArgs(trailingOnly = TRUE)[1], data.table=F)
df = df[,!names(df) %in% c('diag_p','proc_p','admitDT','dischargeDT','PID','diag_p_ccs','proc_p_ccs')]

# thrityday
df2 = df[,!names(df) %in% c('nextCost_b','nextLOS_b')]
logistic(df2, 'thirtyday')
naivebayes(df2, 'thirtyday')
adaboost(df2, 'thirtyday')
decisiontree(df2, 'thirtyday')

# nextCost_b
df2 = df[,!names(df) %in% c('thirtyday','nextLOS_b')]
multinomlogistic(df2, 'nextCost_b')
naivebayes(df2, 'nextCost_b')
adaboost(df2, 'nextCost_b')
decisiontree(df2, 'nextCost_b')

# nextLOS_b
df2 = df[,!names(df) %in% c('nextCost_b','thirtyday')]
multinomlogistic(df2, 'nextLOS_b')
naivebayes(df2, 'nextLOS_b')
adaboost(df2, 'nextLOS_b')
decisiontree(df2, 'nextLOS_b')
