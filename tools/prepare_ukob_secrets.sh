#!/bin/bash
#Super dummy script that takes the UK Open Banking config as input
FILE=$1;
ONE_LINE="${FILE}_oneLine"
ENCODED="${FILE}_encoded"

#Remove all the new lines and then add one at the end (tr removes too much, not being compatible with unix standard)
tr -d '\n ' < $FILE | sed '$s/ $/\n/'  > $ONE_LINE
cat ${ONE_LINE} | base64 > ${ENCODED}
echo "created files $ONE_LINE and $ENCODED"
exit 0
