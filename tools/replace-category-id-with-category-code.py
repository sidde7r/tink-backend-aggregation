import sys, getopt
from sets import Set

if __name__ == "__main__":

  filename = sys.argv[0]
  argv = sys.argv[1:]

  categoriesFile = ''
  categoryColumn = 0
  transactionsFile = ''

  try:
    if not argv:
      raise getopt.GetoptError("No arguments", "")
    opts, args = getopt.getopt(argv,"ht:x:c:",["transactions=","column=","categories="])
  except getopt.GetoptError:
    print help
    sys.exit(2)
  for opt, arg in opts:
    if opt == '-h':
      print '%s --transactions=<transactionsFilename> --column=<categoryColumnIndex> --categories=<categoriesFilename>' % filename
      sys.exit()
    elif opt in ("-t", "--transactions"):
      transactionsFile = arg
    elif opt in ("-c", "--categories"):
      categoriesFile = arg
    elif opt in ("-x", "--column"):
      categoryColumn = int(arg)

  categoriesById = {}

  with open(categoriesFile, 'r') as f:
    for line in f:
      (id, code) = line.strip().split("\t")
      categoriesById[id.replace("-", "")] = code

  with open(transactionsFile, 'r') as f:
    for line in f:
      columns = line.strip().split("\t")
      columns[categoryColumn] = categoriesById[columns[categoryColumn].replace("-", "")]
      print "\t".join(columns)
