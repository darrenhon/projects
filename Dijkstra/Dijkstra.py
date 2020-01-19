import heapq

class Vertex:
  def __init__(self, name):
    self.name = name
    self.edges = set()
  def connect(self, dest, cost):
    e = Edge(self, dest, cost)
    self.edges.add(e)
    return e

class Edge:
  def __init__(self, src, dest, cost):
    self.src = src
    self.dest = dest
    self.cost = cost

class PathCost:
  def __init__(self, previous, cost):
    self.previous = previous
    self.cost = cost

# copied from https://stackoverflow.com/questions/8875706/heapq-with-custom-compare-predicate/8875823
class Heap(object):
   def __init__(self, initial=None, key=lambda x:x):
       self.key = key
       if initial:
           self.data = [(key(item), item) for item in initial]
           heapq.heapify(self.data)
       else:
           self.data = []
   def push(self, item):
       heapq.heappush(self.data, (self.key(item), item))
   def pop(self):
       return heapq.heappop(self.data)[1]

def Dijkstra(es, src):
  costs = {e.dest : PathCost(src, e.cost) for e in src.edges}
  costHeap = Heap(costs, lambda key: costs[key].cost)
  while costHeap.data:
    cheapestV = costHeap.pop()
    for e in cheapestV.edges:
      if e.dest == src:
        continue
      newCost = PathCost(cheapestV, costs[cheapestV].cost + e.cost)
      pushHeap = e.dest not in costs
      if pushHeap or costs[e.dest].cost > newCost.cost:
        costs[e.dest] = newCost
        if pushHeap:
          costHeap.push(e.dest)
  return costs

# the graph from this video: https://www.youtube.com/watch?v=XB4MIexjvY0
# vertices[0] is a dummy placeholder for more readable indices
vertices = [Vertex(str(i)) for i in range(7)]
edges = {
    vertices[1].connect(vertices[2], 50),
    vertices[1].connect(vertices[4], 10),
    vertices[1].connect(vertices[3], 45),
    vertices[2].connect(vertices[4], 15),
    vertices[2].connect(vertices[3], 10),
    vertices[3].connect(vertices[5], 30),
    vertices[4].connect(vertices[1], 10),
    vertices[4].connect(vertices[5], 15),
    vertices[5].connect(vertices[2], 20),
    vertices[5].connect(vertices[3], 35),
    vertices[6].connect(vertices[5], 3)}

costs = Dijkstra(edges, vertices[1])
for k in costs:
  cost = costs[k]
  print(cost.previous.name + "->" + k.name + ":" + str(cost.cost))
