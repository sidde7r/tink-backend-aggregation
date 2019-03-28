#!/bin/bash
#Super dummy script that takes the UK Open Banking config as input
FILE="$1"
ONE_LINE="${FILE}_oneLine"
ENCODED="${FILE}_encoded"

# One possible improvement is to create a help here.



# We are using this command twice as on Linux machones there is wrapping, which should be turned off with -w0 flag
# however for Mac users base64 does not have this flag
# Improvement for that would be to detect system unix/bsd and decide if base64 or base64 -w0 should be used
#REMOVE_WHITESPACES_COMMAND="tr -d '\n ' < $CURRENT_FILE | sed '$s/ $/\n/'"
function remove_all_white_characters(){
    tr -d '\n ' < "$1" > "$2"
    echo "" >> "$2"
}

function encode_input(){
        cat "$1" | base64 > "$2"
}


#Remove all the new lines and then add one at the end (tr removes too much, not being compatible with unix standard)
remove_all_white_characters $FILE $ONE_LINE
encode_input $ONE_LINE "${ENCODED}tmp"
remove_all_white_characters "${ENCODED}tmp" $ENCODED
rm "${ENCODED}tmp"
echo "created files $ONE_LINE and $ENCODED"
exit 0
