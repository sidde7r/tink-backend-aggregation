#!/bin/bash

DUMMY_USER="dummyUser@tink.se"

DATE=`date +"%Y-%m-%d"`
SQL_USER="tink"
SQL_PASSWORD="Fd9wTuuhtqm8yvsA"
SQL_HOST="db-0.internal.tink.se"
OUT_DIR=$HOME
OUT_FILE="${OUT_DIR}/${DATE}_mysql_export.gz"
DUMP_FILE="${OUT_DIR}/${DATE}_mysql_schema_export.sql"

cd $OUT_DIR
mkdir tmp
cd tmp

USER_TABLES="accounts budgets invite_codes statistics tags transactions users_states"
NON_USER_TABLES="categories categorization_rules merchants segments_criterias providers segments categories_translations"

USER_OUTPUT_FILE=${DATE}_user_data_dump.sql
NON_USER_OUTPUT_FILE=${DATE}_non_user_data_dump.sql

if [ -z $1 ]
then
   echo "No user name specified"
   exit
fi

USER_ID=`mysql --user=$SQL_USER --password=$SQL_PASSWORD -h $SQL_HOST tink -s -N -e "SELECT id from tink.users where username='$1'"`

if [ -z $USER_ID ]
then
   echo "User: $1 does not exist"
   exit 1
fi

# exporting schema definitions
echo "Exporting schema definition..."
mysqldump --user=$SQL_USER --password=$SQL_PASSWORD -h $SQL_HOST tink --no-data > ${DUMP_FILE} 

echo "Dumping data for user with ID=${USER_ID}:"
echo "Generic data..."

# static data
for t in $NON_USER_TABLES
do
   echo "   Exporting table $t"
   rm -f ${t}.csv
   mysql --user=$SQL_USER --password=$SQL_PASSWORD -h $SQL_HOST tink -s -N -e "SELECT * FROM $t" > ${OUT_DIR}/tmp/${t}.csv
done

# user specific data
echo "User specific data..."
for t in $USER_TABLES
do
   echo "   Exporting table $t"
   rm -f ${t}.csv
   mysql --user=$SQL_USER --password=$SQL_PASSWORD -h $SQL_HOST tink -s -N -e "SELECT * FROM $t where userid='$USER_ID'" > ${OUT_DIR}/tmp/${t}.csv
done

# cleaned data
echo "Cleaned data..."
echo "   Exporting table: credentials"
rm -f credentials.csv
mysql --user=$SQL_USER --password=$SQL_PASSWORD -h $SQL_HOST tink -s -N -e "SELECT id, providername, 'dummyPublicKey', 'dummySecretKey', 'AUTHENTICATION_ERROR', statuspayload, type, updated, userid, '$DUMMY_USER', additionalinformation, statusupdated, debug, fields, payload FROM credentials where userid='$USER_ID'" > ${OUT_DIR}/tmp/credentials.csv

echo "   Exporting table: users"
rm -f users.csv
mysql --user=$SQL_USER --password=$SQL_PASSWORD -h $SQL_HOST tink -s -N -e "SELECT * FROM users where id='$USER_ID'" > ${OUT_DIR}/tmp/users.csv

echo "Compressing files..."

tar -zcvf $OUT_FILE *.csv
cd ..
rm -r tmp

