import sys

head = None
tail = None
# maps keys to nodes
store = dict()
size = 0

class DoubleLinkedNode:
  def __init__(self, key, value, nxt = None, pre = None):
    self.key = key
    self.value = value
    self.nxt = nxt
    self.pre = pre

def handleSet(cmd):
  if len(cmd) != 3 or size < 1:
    print('ERROR')
    return
  global head, tail
  key = cmd[1]
  value = cmd[2]
  # if it is an existing key, put it to the head
  if key in store:
    node = store[key]
    node.value = value
    # move it to the head only if it is not already the head
    if node != head:
      node.pre.nxt = node.nxt
      if tail == node:
        tail = node.pre
      else:
        node.nxt.pre = node.pre
      node.nxt = head
      head.pre = node
      node.pre = None
      head = node
  else:
    # if it is a new key, add the key to head of the queue
    node = DoubleLinkedNode(key, value, head)
    if (head != None):
      head.pre = node
    head = node
    if (tail == None):
      tail = node
    store[key] = node
    # if it exceeds the size, remove the last (LRU) item
    if len(store) > size:
      remove = tail
      tail.pre.nxt = None
      tail = tail.pre
      del store[remove.key]
  print('SET OK')

def handleGet(cmd):
  if len(cmd) != 2 or size < 1:
    print('ERROR')
    return
  key = cmd[1]
  if key not in store:
    print('NOTFOUND')
    return
  global head, tail
  node = store[key]
  print('GOT ' + node.value)
  # move it to the head only if it is not already the head
  if node != head:
    node.pre.nxt = node.nxt
    if tail == node:
      tail = node.pre
    else:
      node.nxt.pre = node.pre
    node.nxt = head
    head.pre = node
    node.pre = None
    head = node

def handleSize(cmd):
  global size
  # size can be set only once
  if len(cmd) != 2 or size > 0:
    print('ERROR')
    return
  try:
    newsize = int(cmd[1])
    # min is 1. no max
    if newsize < 1:
      print('Error')
    else:
      size = newsize
      print('SIZE OK')
  except:
    print('ERROR')

def main():
  while True:
    sys.stdout.write('>')
    cmd = sys.stdin.readline().strip().split(' ')
    opcode = cmd[0]
    if opcode == 'EXIT':
      return
    elif opcode == 'SIZE':
      handleSize(cmd)
    elif opcode == 'GET':
      handleGet(cmd)
    elif opcode == 'SET':
      handleSet(cmd)
    else:
      print('ERROR')

if __name__ == "__main__":
  main()

