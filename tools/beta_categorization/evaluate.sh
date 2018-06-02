#!/bin/bash
set -ex

if [ "$#" -ne 2 ]; then
    echo "Usage: $0 <country_directory> <categor_training_set_id>"
    exit 1
fi

# Can be retrieved with get_categor_data.py from tink-categor project
categor_file=categor.txt
fasttext=../../bazel-bin/external/fasttext/fasttext
epoch=25
country_dir=$1

yelp_file=$country_dir/yelp_*.txt
echo Evaluating Yelp model against Categor
$fasttext supervised -epoch $epoch -input $yelp_file -output /tmp/yelp
$fasttext test /tmp/yelp.bin $categor_file

osm_file=$country_dir/osm_*.txt
echo Evaluating OpenStreetMap against Categor
$fasttext supervised -epoch $epoch -input $osm_file -output /tmp/osm
$fasttext test /tmp/osm.bin $categor_file

companies_file=$country_dir/*_companies_*.txt
echo Evaluating model based on list of companies against Categor
$fasttext supervised -epoch $epoch -input $companies_file -output /tmp/companies
$fasttext test /tmp/companies.bin $categor_file

echo Evaluating Yelp+OpenStreetMap against Categor
cat $yelp_file $osm_file > /tmp/yelp+osm.txt
$fasttext supervised -epoch $epoch -input /tmp/yelp+osm.txt -output /tmp/yelp+osm
$fasttext test /tmp/yelp+osm.bin $categor_file

echo Evaluating OpenStreetMap+companies against Categor
cat $osm_file $companies_file > /tmp/osm+companies.txt
$fasttext supervised -epoch $epoch -input /tmp/osm+companies.txt -output /tmp/osm+companies
$fasttext test /tmp/osm+companies.bin $categor_file

combined_no=/tmp/yelp+osm+companies
echo Evaluating Yelp+OpenStreetMap+companies against Categor
cat $yelp_file $osm_file $companies_file > $combined_no.txt
$fasttext supervised -epoch $epoch -input $combined_no.txt -output $combined_no
$fasttext test $combined_no.bin $categor_file

global_file=global_data/*
echo Evaluating global and OpenStreetMap models combined
cat $osm_file $global_file > /tmp/osm+global.txt
$fasttext supervised -epoch $epoch -input /tmp/osm+global.txt -output /tmp/osm+global
$fasttext test /tmp/osm+global.bin $categor_file

combined_file=/tmp/combined.txt
echo Evaluating global and all Norwegian models combined
cat $combined_no.txt $global_file > $combined_file
$fasttext supervised -epoch $epoch -input $combined_file -output /tmp/combined
$fasttext test /tmp/combined.bin $categor_file

testing_set=1000

# Training set size is hardcoded as bash doesn't support variables in range expnasions
for i in {500..3000..500}
do
    echo Evaluating the combined model with $i Categor datapoints
    head -n$i $categor_file > /tmp/categor-$i.txt
    cat /tmp/combined.txt /tmp/categor-$i.txt > /tmp/combined+$i.txt
    $fasttext supervised -epoch $epoch -input /tmp/combined+$i.txt -output /tmp/combined+$i
    tail -n $testing_set $categor_file | $fasttext test /tmp/combined+$i.bin -
done

for i in {500..3000..500}
do
    echo Evaluating OpenStreetMap combined with global model with $i Categor datapoints
    head -n$i $categor_file > /tmp/categor-$i.txt
    cat $osm_file $global_file /tmp/categor-$i.txt > /tmp/osm+global+categor-$i.txt
    $fasttext supervised -epoch $epoch -input /tmp/osm+global+categor-$i.txt -output \
      /tmp/osm+global+categor-$i
    tail -n $testing_set $categor_file | $fasttext test /tmp/osm+global+categor-$i.bin -
done
