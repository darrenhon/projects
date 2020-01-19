class Node:
  def __init__(self, name):
    self.name = name
    self.nxt = None

def reverse(head):
  previous = None
  current = head
  nxt = None
  while current is not None:
    nxt = current.nxt
    current.nxt = previous
    previous = current
    current = nxt
  return previous

a, b, c, d, e, f = [Node(chr(i)) for i in range(97, 97 + 6)]
a.nxt = b
b.nxt = c
c.nxt = d
d.nxt = e
e.nxt = f

node = reverse(a)
while node is not None:
  print(node.name)
  node = node.nxt
