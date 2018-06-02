#!/bin/bash -e

DIR_LOCAL=$1
FILE=$2
FILE_S3=$3
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
   FILE_S3=$FILE
fi

echo "Fetching $FILE_S3 from S3"

s3cmd get -f ${BUCKET_S3}/$FILE_S3 ${DIR_LOCAL}/$FILE
