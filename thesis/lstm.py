import numpy
import math
import sys
import tensorflow as tf
from keras.models import Sequential
from keras.layers import Dense
from keras.layers import LSTM
from keras.layers import Masking
from keras.layers import Embedding
from keras import backend
from sklearn.preprocessing import MinMaxScaler
from sklearn.metrics import roc_auc_score

trainpath = sys.argv[1]
testpath = sys.argv[2]
gpu = int(sys.argv[3])
masking = sys.argv[4] if len(sys.argv) > 4 else ''
loss = sys.argv[5] if len(sys.argv) > 5 else ''

print(trainpath)
print(testpath)
print(gpu)
print(masking)
print(loss)

config = tf.ConfigProto(device_count = {'GPU':gpu})
backend.set_session(tf.Session(config=config))

numpy.random.seed(7)

def getXYFromCsv(filename):
  with open(filename,'r') as fin:
    df = [[int(x) + 1 for x in line.strip().split(',')] for line in fin]
  da = [[0] * (20 - len(x)) + x[-20:] for x in df]
  da = numpy.array(da).astype('float32')
  x = da[:,:-1]
  y = da[:,-1]
  # Embedding takes 2D array
  # Normal LSTM and Masking takes 3D array
  xcube = x if masking == 'emb' else numpy.reshape(x, (x.shape[0], x.shape[1], 1))  
  return xcube, y

trainx, trainy = getXYFromCsv(trainpath)
testx, testy = getXYFromCsv(testpath)

model = Sequential()
if masking == 'emb':
  model.add(Embedding(3, 1, mask_zero=True))
elif masking == 'mask':
  model.add(Masking(mask_value=-1, input_shape=(19, 1)))
model.add(LSTM(4, input_dim=1, ))
model.add(Dense(1))
model.compile(loss='mean_squared_error' if loss == '' else loss, optimizer='adam')
model.fit(trainx, trainy, batch_size=32, nb_epoch=10, verbose=2)

pdt = model.predict(testx)
print('auc = ', roc_auc_score(testy - 1, pdt - 1))
