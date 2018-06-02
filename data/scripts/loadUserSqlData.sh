#!/bin/bash

DATE=`date +"%Y-%m-%d"`
SQL_USER="root"
SQL_PASSWORD=
HERE=`pwd`

if [ -z $1 ]
then
   echo "No input specified"
   echo "Should look like this:"
   echo "   $0 <date>_mysql_export.gz <date>schema_def.sql"   
   exit
fi

if [ -z $2 ]
then
   echo "No input specified"
   echo "Should look like this:"
   echo "   $0 <date>_mysql_export.gz <date>schema_def.sql"   
   exit
fi


IN_FILE=$1
SCHEMA_DEF_FILE=$2

echo "Unzipping $IN_FILE"
mkdir tmp
cd tmp
tar -zxvf ../$IN_FILE

FILES=`ls *.csv`
FULL_PATH="$HERE/tmp"

# insert schema definitiaon
mysql --user=$SQL_USER --password=$SQL_PASSWORD tink < ../$SCHEMA_DEF_FILE 

echo "Inserting files into db..."
for file in $FILES
do
   table_name=`echo $file | cut -d'.' -f 1`
   echo "Inserting ${FULL_PATH}/${file} into $table_name"
   chmod 777 $file
   sed 's/NULL/\\N/g' $file > ${file}_new
   mysql --user=$SQL_USER --password=$SQL_PASSWORD tink -s -N -e "LOAD DATA INFILE '${FULL_PATH}/${file}_new' INTO TABLE $table_name FIELDS TERMINATED BY '\t'"
done

cd ..
rm -r tmp

