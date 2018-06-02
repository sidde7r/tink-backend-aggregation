#! /bin/sh

timestamp=$(date +%s)
area=$1
output_file=yelp_${country}_${timestamp}.txt

python3 get_yelp_graphql_data.py --area ${area} --fasttext-category expenses:home.mortgage --yelp-categories mortgagebrokers --out ${output_file} --lower --limit 500
python3 get_yelp_graphql_data.py --area ${area} --fasttext-category expenses.home.rent --yelp-categories apartments --out ${output_file} --lower --limit 500
python3 get_yelp_graphql_data.py --area ${area} --fasttext-category expenses:home.incurences-fees --yelp-categories homeinsurance --out ${output_file} --lower --limit 500

python3 get_yelp_graphql_data.py --area ${area} --fasttext-category expenses:house.garden --yelp-categories gardening --out ${output_file} --lower --limit 500
python3 get_yelp_graphql_data.py --area ${area} --fasttext-category expenses:house.fitment --yelp-categories homedecor --out ${output_file} --lower --limit 500

python3 get_yelp_graphql_data.py --area ${area} --fasttext-category expenses:wellness.healthcare --yelp-categories health --out ${output_file} --lower --limit 500
python3 get_yelp_graphql_data.py --area ${area} --fasttext-category expenses:wellness.beauty --yelp-categories beautysvc --out ${output_file} --lower --limit 500

python3 get_yelp_graphql_data.py --area ${area} --fasttext-category expenses:transport.car --yelp-categories auto --out ${output_file} --lower --limit 500
python3 get_yelp_graphql_data.py --area ${area} --fasttext-category expenses:transport.publictransport --yelp-categories publictransport --out ${output_file} --lower --limit 500

python3 get_yelp_graphql_data.py --area ${area} --fasttext-category expenses:shopping.clothes --yelp-categories fashion --out ${output_file} --lower --limit 500
python3 get_yelp_graphql_data.py --area ${area} --fasttext-category expenses:shopping.electronics --yelp-categories electronics --out ${output_file} --lower --limit 500
python3 get_yelp_graphql_data.py --area ${area} --fasttext-category expenses:shopping.books --yelp-categories bookstores --out ${output_file} --lower --limit 500
python3 get_yelp_graphql_data.py --area ${area} --fasttext-category expenses:shopping.gifts --yelp-categories giftshops --out ${output_file} --lower --limit 500
python3 get_yelp_graphql_data.py --area ${area} --fasttext-category expenses:shopping.hobby --yelp-categories hobbyshops --out ${output_file} --lower --limit 500

python3 get_yelp_graphql_data.py --area ${area} --fasttext-category expenses:entertainment.sport --yelp-categories active --out ${output_file} --lower --limit 500