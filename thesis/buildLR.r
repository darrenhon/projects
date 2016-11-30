target = commandArgs(trailingOnly = TRUE)[1]
path = commandArgs(trailingOnly = TRUE)[2]
pathmodel = commandArgs(trailingOnly = TRUE)[3]

library(data.table)
library(rpart)
library(pROC)

df = fread(path, data.table=F)

# remove unused variables
todelete = c('admitDT', 'dischargeDT', 'PID', 'nextCost', 'nextLOS','LOS_b','cost_b', 'thirtyday', 'nextLOS_b', 'nextCost_b')
todelete = todelete[todelete != target]
df = df[,!names(df) %in% todelete]

# define feature sets
fdemo = c('agyradm', 'gender', 'race_grp')
fclos = c('cost', 'LOS')
fadmin = c('schedule', 'srcsite', 'srcroute', 'msdrg_severity_ill', 'type_care', 'sameday', 'oshpd_destination', 'merged')
fcom = names(df)[grepl('ch_com', names(df))]
fcum = c('coms', 'cons', 'er6m', 'adms', 'lace')

allfeats = c(fdemo, fclos, fadmin, fcom, fcum)

# turn variables into factor
facCol = c(target, 'type_care','gender','srcsite','srcroute','schedule','oshpd_destination','race_grp','msdrg_severity_ill','sameday', 'merged', fcom)
for (col in facCol) df[,col] = as.factor(df[,col])

lr = glm(as.formula(paste(target,'~.',sep='')), df, family=binomial())
saveRDS(lr, pathmodel)
