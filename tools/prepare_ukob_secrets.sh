#!/bin/bash
FILE=$1;
ONE_LINE="${FILE}_oneLine"
ENCODED="${FILE}_encoded"

tr -d '\n ' < $FILE | sed '$s/ $/\n/'  > $ONE_LINE
cat ${ONE_LINE} | base64 > ${ENCODED}
echo "created files $ONE_LINE and $ENCODED"
exit 0
