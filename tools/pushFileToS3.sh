#!/bin/bash -e

DIR_LOCAL=$1
FILE=$2
FILE_S3=$3
NO_DECRYPT=$4

BUCKET_S3=s3://tink-categorization

if [ -z $DIR_LOCAL ]; then
   echo "no dir supplied"
   exit
fi

if [ -z $FILE ]; then
   echo "no file supplied"
   exit
fi

if [ -z $FILE_S3 ]; then
   echo "no target file supplied"
   exit 
fi

echo "Sending $FILE_S3 to S3"

if [ -z $NO_DECRYPT ]; then
	echo "   with encryption"
   s3cmd put -e ${DIR_LOCAL}/$FILE ${BUCKET_S3}/$FILE_S3   
else
   echo "   no encryption"
   s3cmd put ${DIR_LOCAL}/$FILE ${BUCKET_S3}/$FILE_S3
fi
