from queue import Queue
from collections import deque
import heapq

class Vertex:
  def __init__(self, name):
    self.name = name
    self.edges = set()
  def connect(self, dest, cost):
    e = Edge(self, dest, cost)
    self.edges.add(e)
    dest.edges.add(e)
    return e

class Edge:
  def __init__(self, src, dest, cost):
    self.src = src
    self.dest = dest
    self.cost = cost

# copied from https://stackoverflow.com/questions/8875706/heapq-with-custom-compare-predicate/8875823
class Heap(object):
   def __init__(self, initial=None, key=lambda x:x):
       self.key = key
       if initial:
           self._data = [(key(item), item) for item in initial]
           heapq.heapify(self._data)
       else:
           self._data = []
   def push(self, item):
       heapq.heappush(self._data, (self.key(item), item))
   def pop(self):
       return heapq.heappop(self._data)[1]
    
def Prim(vs, es):
  minEdge = min(es, key = lambda e: e.cost)
  resultEs = set([minEdge])
  resultVs = set([minEdge.src, minEdge.dest])
  reaches = Heap(set([e for v in resultVs for e in v.edges]) - resultEs, key = lambda e: e.cost)
  while len(resultEs) != len(vs) - 1:
    newEdge = reaches.pop()
    newVertex = newEdge.dest if newEdge.src in resultVs else newEdge.src
    resultEs.add(newEdge)
    resultVs.add(newVertex)
    for e in filter(lambda e: e.src not in resultVs or e.dest not in resultVs, newVertex.edges):
      reaches.push(e)
  return resultEs

def Kruskal(vs, es):
  heap = Heap(es, lambda e: e.cost)
  resultEs = set()
  vToTrees = {}
  while len(resultEs) != len(vs) - 1:
    newEdge = heap.pop()
    t1 = vToTrees[newEdge.src] if newEdge.src in vToTrees else None
    t2 = vToTrees[newEdge.dest] if newEdge.dest in vToTrees else None
    if t1 == t2 == None:
      vToTrees[newEdge.src] = vToTrees[newEdge.dest] = {newEdge.src, newEdge.dest}
    elif t1 == t2:
      # it's a cycle
      continue
    elif t1 == None:
      t2.add(newEdge.src)
      vToTrees[newEdge.src] = t2
    elif t2 == None:
      t1.add(newEdge.dest)
      vToTrees[newEdge.dest] = t1
    else:
      t1.update(t2)
      for v in t2:
        vToTrees[v] = t1
    resultEs.add(newEdge)
  return resultEs

# the graph from this video: https://www.youtube.com/watch?v=4ZlRH0eK-qQ
# vertices[0] is a dummy placeholder for more readable indices
vertices = [Vertex(str(i)) for i in range(8)]
edges = { 
    vertices[1].connect(vertices[2], 28),
    vertices[1].connect(vertices[6], 10),
    vertices[2].connect(vertices[7], 14),
    vertices[2].connect(vertices[3], 16),
    vertices[3].connect(vertices[4], 12),
    vertices[4].connect(vertices[7], 18),
    vertices[4].connect(vertices[5], 22),
    vertices[5].connect(vertices[7], 24),
    vertices[5].connect(vertices[6], 25)}
del vertices[0]

[print(e.src.name + "-" + e.dest.name + ":" + str(e.cost)) for e in Prim(vertices, edges)]
[print(e.src.name + "-" + e.dest.name + ":" + str(e.cost)) for e in Kruskal(vertices, edges)]
