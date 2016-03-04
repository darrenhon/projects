InformationGain <- function( tble ) {
tble <- as.data.frame.matrix(tble)
entropyBefore <- Entropy(colSums(tble))
s <- rowSums(tble)
entropyAfter <- sum (s / sum(s) * apply(tble, MARGIN = 1, FUN = Entropy ))
informationGain <- entropyBefore - entropyAfter
return (informationGain)
}

Entropy <- function( vls ) {
  res <- vls/sum(vls) * log2(vls/sum(vls))
  res[vls == 0] <- 0
  -sum(res)
}

# everything is hard-coded
calculateIg <- function()
{
  data = readRDS('OSHPD_CLEAN_LOS.rds')
  targetCol = 98

  # convert NA to 0
  for (i in 100:647)
  {
    data[,i][is.na(data[,i])] = 0
  }

  # stupid steps to create a data frame to hold column names and information gain
  ig = data.frame(col=character(0), ig = character(0))
  ig = rbind(ig, c(names(data)[1], InformationGain(table(data[,c(1,targetCol)]))))
  names(ig) = c('col', 'ig')
  ig$col = as.character(ig$col)
  ig$ig = as.character(ig$ig)

  for (i in 2:ncol(data))
  {
    thisig = InformationGain(table(data[,c(i,targetCol)]))
    colname = names(data)[i]
    ig = rbind(ig, c(colname, thisig))
    print(paste(colname, thisig))
  }
  ig$ig = as.numeric(ig$ig)
  ig=ig[with(ig,order(-ig)),]
  write.csv(ig, 'ig.csv', quote=F, row.names=F)
}
