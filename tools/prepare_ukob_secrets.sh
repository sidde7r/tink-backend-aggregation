#!/bin/bash

heredoc(){
cat <<EOL

    -------------------------------------------------------------------------
    NAME:
            prepare_ukob_secrets.sh

    DESCRIPTION:
            Takes json file from standard input and converts it to a oneliner
            and encodes another file with base64

    USAGE:
            Execute:          \$ $0 [file.json]
            Help:             \$ $0 -h --help

    EXAMPLE:
            \$ $0 ukob.json

    HELP:
            Contact Integration Team

    CREATOR:
            Adriana Rychlinska
            Love Bååk
    -------------------------------------------------------------------------

EOL
exit 0
}

FILE="$1"
ONE_LINE="${FILE}_oneLine"
ENCODED="${FILE}_encoded"
SYSTEM_KERNEL=$(uname)

case $1 in
    "-h" )
    heredoc
        ;;
    "--help" )
    heredoc
        ;;
esac

if [[ $FILE != *.json ]]; then
    echo "Unrecognized file format"
    echo "Use -h or --help for usage information"
    exit 1
fi

# Remove all the new lines and then add one at the end (tr removes too much, not being compatible with unix standard)
remove_all_white_characters(){
    tr -d '\n ' < "$1" > "$2"
}

if [[ ! $SYSTEM_KERNEL == "Darwin" ]]; then
    encode_input(){
        cat "$1" | base64 -w0 > "$2"
    }
else
    encode_input(){
        cat "$1" | base64 > "$2"
    }
fi

remove_all_white_characters $FILE $ONE_LINE
# Adding new line to end of file
echo "" >> "$ONE_LINE"
encode_input $ONE_LINE "${ENCODED}"

if [[ -f "$ONE_LINE" ]] && [[ -f "$ENCODED" ]]; then
    echo "created files $ONE_LINE and $ENCODED"
else
    echo "Something went wrong"
    exit 1
fi


exit 0
