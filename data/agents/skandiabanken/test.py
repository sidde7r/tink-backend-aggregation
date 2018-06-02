# Basic test that makes sure that transactions are perfectly cycled and not random in order.
import json

txs = []
with open('test_transactions.txt') as f:
  for line in f:
    obj = json.loads(line)
    txs += obj['transactions']

simplifiedList = [(tx['transactionState'], tx['amount'], tx['merchant'], tx['timestamp'], tx.get('date', 'missing'), tx['settled']) for tx in txs]

previous = -1
for i, e in sorted(enumerate(simplifiedList), key=lambda x: (x[1], x[0])):
  print "diff={0}".format(previous-i), i, e
  previous = i
