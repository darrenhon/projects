This is a short description of the code.

The tcss555 in this folder is the main script. It runs only on likes models. It calls methods in likePredict.r to do prediction.
likePredict.r acts as a bridge between R and Python. It calls functions from knn.py.
knn.py holds both the knn model and proprietary model. knn functions has
'knn' in their names and proprietary model functions have 'weightedAverage' in their names. 
For more information, go to each source file and read comments on each
function.