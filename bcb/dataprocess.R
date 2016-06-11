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
genagg = aggregate(gender~PID, FUN=function(vals) vals[1], data=df2)
# race aggregation
racagg = aggregate(race_grp~PID, FUN=function(vals) vals[1], data=df2)


