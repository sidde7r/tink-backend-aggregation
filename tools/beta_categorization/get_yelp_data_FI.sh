#! /bin/sh

timestamp=$(date +%s)
country=Finland
output_file=yelp_${country}_${timestamp}.txt

python3 get_yelp_data.py --country $country --fasttext-category expenses:home.mortgage --yelp-category mortgagebrokers --out ${output_file} --lower

python3 get_yelp_data.py --country $country --fasttext-category expenses.home.rent --yelp-category apartments --out ${output_file} --lower

python3 get_yelp_data.py --country $country --fasttext-category expenses:home.incurences-fees --yelp-category homeinsurance --out ${output_file} --lower

python3 get_yelp_data.py --country $country --fasttext-category expenses:wellness.healthcare --yelp-category health --out ${output_file} --lower

python3 get_yelp_data.py --country $country --fasttext-category expenses:wellness.beauty --yelp-category beautysvc --out ${output_file} --lower

python3 get_yelp_data.py --country $country --fasttext-category expenses:transport.car --yelp-category auto --out ${output_file} --lower

python3 get_yelp_data.py --country $country --fasttext-category expenses:transport.publictransport --yelp-category publictransport --out ${output_file} --lower

python3 get_yelp_data.py --country $country --fasttext-category expenses:house.garden --yelp-category gardening --out ${output_file} --lower

python3 get_yelp_data.py --country $country --fasttext-category expenses:house.fitment --yelp-category homedecor --out ${output_file} --lower

python3 get_yelp_data.py --country $country --fasttext-category expenses:shopping.clothes --yelp-category fashion --out ${output_file} --lower

python3 get_yelp_data.py --country $country --fasttext-category expenses:shopping.electronics --yelp-category electronics --out ${output_file} --lower

python3 get_yelp_data.py --country $country --fasttext-category expenses:shopping.books --yelp-category bookstores --out ${output_file} --lower

python3 get_yelp_data.py --country $country --fasttext-category expenses:shopping.gifts --yelp-category giftshops --out ${output_file} --lower

python3 get_yelp_data.py --country $country --fasttext-category expenses:shopping.hobby --yelp-category hobbyshops --out ${output_file} --lower

python3 get_yelp_data.py --country $country --fasttext-category expenses:entertainment.sport --yelp-category active --out ${output_file} --lower