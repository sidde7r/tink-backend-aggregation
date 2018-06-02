#!/bin/bash
function printUsage {
	echo "$0 <firstRunName> <secondeRunName>"
}

FIRST_RUN_NAME=$1
SECOND_RUN_NAME=$2

if [ -z $FIRST_RUN_NAME ]
then
	echo "Have to specify firstRunName"
	printUsage
	exit
fi

if [ -z $SECOND_RUN_NAME ]
then
	echo "Have to specify secondRunName"
	printUsage
	exit
fi

# Jump into the test directory
cd tink-backend-agent-tests

# List all files to go between run names
FIRST_FILES=`ls ${FIRST_RUN_NAME}*`

rm -r results
mkdir -p results

a=1
for FILE_1 in $FIRST_FILES
do
	OUT_FILE=${FILE_1:${#FIRST_RUN_NAME}+1}
	FILE_2=$SECOND_RUN_NAME'_'$OUT_FILE

	echo "Diffing files: $FILE_1    $FILE_2"
	IS_DIFF=`diff -q ${FILE_1} ${FILE_2}`

	if [ ${#IS_DIFF} -eq 0 ]
	then
		echo "No diff"
	else
		diff -U 0 ${FILE_1} ${FILE_2} > results/${OUT_FILE}.diff
	fi
	let "a += 1"
done
