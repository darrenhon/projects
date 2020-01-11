from random import randint

def bust(points):
  return points > 21

class Player:
  def __init__(self, name, points = 0, hasA = False, has10 = False):
    self.name = name
    self.points = points
    self.hasA = False
    self.has10 = False
  def reached17(self):
    # Hit 17 rule
    if self.hasA and self.pointsA() == 17: return False
    return self.highPoints() >= 17
  def pointsA(self):
    return self.points + 10
  def bustA(self):
    return bust(self.pointsA())
  def bust(self):
    return bust(self.points)
  def highPoints(self):
    return self.pointsA() if self.hasA and not self.bustA() else self.points
  def blackJack(self):
    return self.hasA and self.has10
  def hit(self):
    num = min(randint(1, 13), 10)
    if num == 1: self.hasA = True
    if num == 10: self.has10 = True
    self.points += num
    #print(self.name + ":" + str(self.points) + ("A" if self.hasA else "") + "(" + str(num) + ")")

class BlackJack:
  def __init__(self, player, dealer):
    self.player = player
    self.dealer = dealer
  def play(self):
    #self.player.hit()
    while (True):
      if self.player.bust(): return "Dealer"
      if self.dealer.bust(): return "Player"
      if self.dealer.reached17(): return self.conclude()
      self.dealer.hit()
  def conclude(self):
    if self.player.blackJack() and not self.dealer.blackJack(): return "Player"
    if not self.player.blackJack() and self.dealer.blackJack(): return "Dealer"
    if self.player.highPoints() > self.dealer.highPoints(): return "Player"
    if self.player.highPoints() < self.dealer.highPoints(): return "Dealer"
    return "Push"

def calcWin(name, numGames):
  i = 0
  win = 0
  while i < numGames:
    i += 1
    player = Player("Player", 12)
    dealer = Player("Dealer", 5)
    if BlackJack(player, dealer).play() == name:
      win += 1
  return win / numGames * 100

print(calcWin("Player", 1000000))

