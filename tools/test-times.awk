# 1. Store all test XML files before in `before` directory.
# 2. Store all test XML files after in `after` directory.
# 3. Execute:
#
#        join <(awk -f times.awk before/*|sort) <(awk -f times.awk after/*|sort)|awk '{print $0,$3-$2}'|sort -k 4 -n
#
#    First column is the name of the test. Second column is test duration
#    before. Third column is test duration after. Fourth column is how much
#    test duration increased. The rows will be listed according to test
#    duration difference ascending.

/<testsuite/ {
  sub(/name=/, "", $2)
  sub(/'/, "", $2)
  sub(/'/, "", $2)
  testsuite=$2
}

/<testcase/ {
  sub(/name=/, "", $2)
  sub(/'/, "", $2)
  sub(/'/, "", $2)
  name=$2

  duration=$(NF-1)
  sub(/time=/, "", duration)
  sub(/'/, "", duration)
  sub(/'/, "", duration)

  print testsuite ":" name, duration
}
