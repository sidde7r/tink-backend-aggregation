#!/bin/bash
set -euo pipefail

FILE=$1
LINES=$(cat $FILE|grep -Eo 'Submission to NSQ took: [0-9.]* ms'|wc -l)

echo Number of samples: $LINES

# Not doing rounding here. Bash to blame.
PERC=$((99*LINES/100))
echo Sample place for 99th percentile: $PERC

echo -n "99th percentile: "
cat $FILE|grep -Eo 'Submission to NSQ took: [0-9.]* ms'|grep -Eo '[0-9.]* ms'|sed 's/ ms//'|sort -n|awk "NR==$PERC {print}"
