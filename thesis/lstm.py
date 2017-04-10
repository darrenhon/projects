import numpy
import math
from keras.models import Sequential
from keras.layers import Dense
from keras.layers import LSTM
from sklearn.preprocessing import MinMaxScaler
from sklearn.metrics import roc_auc_score

numpy.random.seed(7)

def getXYFromCsv(filename):
  with open(filename,'r') as fin:
    df = [[int(x) for x in line.strip().split(',')] for line in fin]
  da = [[-1] * (20 - len(x)) + x[:20] for x in df]
  da = numpy.array(da).astype('float32')
  x = da[:,:-1]
  y = da[:,-1]
  xcube = numpy.reshape(x, (x.shape[0], x.shape[1], 1))
  return xcube, y

trainx, trainy = getXYFromCsv('training.csv')
testx, testy = getXYFromCsv('testing.csv')

model = Sequential()
model.add(LSTM(4, input_dim=1))
model.add(Dense(1))
model.compile(loss='mean_squared_error', optimizer='adam')
model.fit(trainx, trainy, batch_size=32, nb_epoch=10, verbose=2)

pdt = model.predict(testx)
print('auc = ', roc_auc_score(testy, pdt))
