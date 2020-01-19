from queue import Queue
from collections import deque

class Vertex:
  def __init__(self, name):
    self.name = name
    self.connected = set()
  def connect(self, u):
    self.connected.add(u)
    u.connected.add(self)

def BFS(start, dest):
  visited = set([start])
  toExplore = Queue()
  toExplore.put(start)
  parent = {}
  while not toExplore.empty():
    v = toExplore.get()
    for u in v.connected - visited:
      parent[u] = v
      if u == dest:
        path = [dest]
        while u != start:
          u = parent[u]
          path.insert(0, u)
        return path
      toExplore.put(u)
      visited.add(u)
  return []

def DFS(start, dest):
  visited = set([start])
  toExplore = deque([start])
  while len(toExplore) > 0:
    v = toExplore[-1]
    unvisited = v.connected - visited
    if len(unvisited) > 0:
      u = unvisited.pop()
      if u == dest:
        return list(toExplore) + [dest]
      visited.add(u)
      toExplore.append(u)
      continue
    toExplore.pop()
  return []

# the graph in this video: https://www.youtube.com/watch?v=bIA8HEEUxZI
a, b, c, d, e, f, g, h = (Vertex(chr(i)) for i in range(97, 97 + 8))
[a.connect(i) for i in (b,d,g)]
[e.connect(i) for i in (b,g)]
[f.connect(i) for i in (b,d,c)]
c.connect(h)

[print(i.name) for i in BFS(a, h)]
[print(i.name) for i in DFS(a, h)]
