library(data.table)

path = commandArgs(trailingOnly = TRUE)[1]
df = fread(path, data.table=F)

# remove patients with only 1 admission
tb = table(df$PID)
pids = names(tb[tb==1])
df = df[!df$PID %in% pids,]

# patients age aggregation
ageagg = aggregate(agyradm~PID, FUN=min, data=df)
# gender aggregation
genagg = aggregate(gender~PID, FUN=function(vals) vals[1], data=df)
# race aggregation
racagg = aggregate(race_grp~PID, FUN=function(vals) vals[1], data=df)

# plot heatmap
library(gplots)
tb = table(df$cost_b, df$LOS_b)
tb = tb[order(rownames(tb), decreasing=T),]
heatmap.2(tb, dendrogram='none', Rowv='none', Colv='none', cexRow=2, cexCol=2, col=grey(16:1/16), trace='none', xlab='LOS in Buckets', ylab='Cost in Buckets')

# plot seq-len distribution
plot(log10(table(table(df$PID))), ylab='Number of Patients(Log10)', xlab='Number of Admissions', cex.lab=1.5, cex.axis=1.5)

# plot cost distribution
myhist = hist(df$cost/100000, breaks=500)
non = which(myhist$counts == 0)
myhist$counts = myhist$counts[-non]
myhist$breaks = myhist$breaks[-non]
plot(y=log10(myhist$counts), x=myhist$breaks[-1], type='h', xlab='Admission Cost(100K)', ylab='Number of Patients(Log10)', cex.lab=1.5, cex.axis=1.5)
