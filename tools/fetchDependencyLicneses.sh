#!/bin/bash

set -e

export PYTHONIOENCODING=utf8

# There may be a limit on X number of request per day and hour for the API key. 
# You might need to re-generate the key on https://www.versioneye.com/settings/api
# If worst comes to worst, you have to run the script multiple times and 
# remove already ran dependencies in the WORKSPACE file temporary in between runs.

API_KEY="6db2a82a93519d00fcfd"
IN_FILE="../WORKSPACE"
OUT_FILE="dependencies_and_licenses.txt"
BASE_URL="https://www.versioneye.com/api/v2/products"
HEADERS=(--silent --header \"Accept: application/json\")

echo "---------- STARTING ----------"

rm -f $OUT_FILE


grep "sha1" $IN_FILE | awk '
		BEGIN {
			FS="\""
		}	
		{
			print $2, " "
		}
	' > shas.txt

SHAS="$(< shas.txt)";

for i in $SHAS
do
	echo "Doing sha $i";

	# Query details about dependency based on sha. 

	response=$(curl "${HEADERS[@]}" "$BASE_URL/sha/$i?api_key=$API_KEY");

	: '
	 Example of response: 
	 [
	  {
	    "language": "Java",
	    "prod_key": "com.boundary/high-scale-lib",
	    "version": "1.0.6",
	    "group_id": "com.boundary",
	    "artifact_id": "high-scale-lib",
	    "classifier": null,
	    "packaging": "jar",
	    "prod_type": "Maven2",
	    "sha_value": "7b44147cb2729e1724d2d46d7b932c56b65087f0",
	    "sha_method": "sha1"
	  }
	]
	 '

	product_key=$(echo "$response" | jq -r '.[0].prod_key');
	product_key_escaped=$(echo "$product_key" | sed -e 's,\.,~,g' | sed -e 's,/,:,g');
	language=$(echo "$response" | jq -r '.[0].language');

	# From the details, fetch license. 

	license=$(curl "${HEADERS[@]}" "$BASE_URL/$language/$product_key_escaped?api_key=$API_KEY" | jq -r '.license_info');

	echo "License is $license"

	echo "$product_key	$license" >> $OUT_FILE;

done
 
rm shas.txt

echo "---------- DONE! ----------"
