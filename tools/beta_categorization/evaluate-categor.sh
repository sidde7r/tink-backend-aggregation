#!/bin/bash
set -euo pipefail

# Evaluates quality of categorization with different amount of Categor data
# used. Each slice of Categor data is split 70/30, the larger part is used for
# training and the smaller for testing.
# The script is based on evaluate.sh but expects the source dataset to to
# already in place.
# Global data is expected to be in $global_dir, country-specific data in
# $country directory (one of $countries), and Categor data in
# $country-categor.txt (can be obtained with get_categor_data.py from
# tink-categor project).

global_dir=global
countries="norway finland denmark"
fasttext=../../bazel-bin/external/fasttext/fasttext
epoch=25

for country in $countries; do
	echo "Baseline for $country (no Categor data):"
	cat $global_dir/* $country/* > $country-combined.txt
	$fasttext supervised -epoch $epoch -input $country-combined.txt -output $country
	$fasttext test $country.bin $country-categor.txt

	categor_size=$(wc -l $country-categor.txt | cut -d ' ' -f 1)
	slice_size=1000
	while [ $slice_size -le $(( categor_size + 1000 )) ]; do
		echo "Using $slice_size Categor samples"
		head -n $slice_size $country-categor.txt > $country-$slice_size.txt 
		split -l $(( slice_size * 70 / 100 )) $country-$slice_size.txt

		cat $global_dir/* $country/* xaa > $country-combined.txt
		$fasttext supervised -epoch $epoch -input $country-combined.txt -output $country
		$fasttext test $country.bin xab

		rm xaa xab $country-$slice_size.txt $country-combined.txt
		slice_size=$(( slice_size + 1000 ))
	done
done
