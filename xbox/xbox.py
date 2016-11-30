import sys, socket, select, time

def wake(address, liveid):
  with socket.socket(socket.AF_INET, socket.SOCK_DGRAM) as s:
    s.setblocking(0)
    s.bind(("", 0))
    s.connect((address, 5050))
    payload = bytes([0, len(liveid)]) + liveid.encode('utf-8') + bytes([0])
    packet = bytes([221, 2, 0, len(payload), 0, 0]) + payload
    s.send(packet)

def ping(address):
  with socket.socket(socket.AF_INET, socket.SOCK_DGRAM) as s:
    s.setblocking(0)
    s.bind(("", 0))
    s.connect((address, 5050))
    s.send(bytearray.fromhex("dd00000a000000000000000400000002"))
    return select.select([s], [], [], 3)[0]

if __name__ == "__main__":
  wake(sys.argv[1], sys.argv[2])
  time.sleep(5)
  print("Success" if ping(sys.argv[1]) else "Failure")
