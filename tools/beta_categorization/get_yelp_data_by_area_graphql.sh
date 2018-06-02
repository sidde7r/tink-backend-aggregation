#!/bin/sh
set -e

timestamp=$(date +%s)
area=$1
output_file=yelp_${area}_${timestamp}.txt

# FOOD
python3 get_yelp_graphql_data.py --area ${area} --fasttext-category expenses:food.restaurants --yelp-categories restaurants --out ${output_file} --lower
python3 get_yelp_graphql_data.py --area ${area} --fasttext-category expenses:food.restaurants --yelp-categories breakfast_brunch --out ${output_file} --lower
python3 get_yelp_graphql_data.py --area ${area} --fasttext-category expenses:food.restaurants --yelp-categories food_court --out ${output_file} --lower
python3 get_yelp_graphql_data.py --area ${area} --fasttext-category expenses:food.restaurants --yelp-categories foodstands --out ${output_file} --lower
python3 get_yelp_graphql_data.py --area ${area} --fasttext-category expenses:food.restaurants --yelp-categories popuprestaurants --out ${output_file} --lower
python3 get_yelp_graphql_data.py --area ${area} --fasttext-category expenses:food.coffee --yelp-categories cafes --out ${output_file} --lower
python3 get_yelp_graphql_data.py --area ${area} --fasttext-category expenses:food.coffee --yelp-categories bakeries --out ${output_file} --lower
python3 get_yelp_graphql_data.py --area ${area} --fasttext-category expenses:food.coffee --yelp-categories desserts --out ${output_file} --lower
python3 get_yelp_graphql_data.py --area ${area} --fasttext-category expenses:food.coffee --yelp-categories icecream --out ${output_file} --lower
python3 get_yelp_graphql_data.py --area ${area} --fasttext-category expenses:food.coffee --yelp-categories coffeeroasteries --out ${output_file} --lower
python3 get_yelp_graphql_data.py --area ${area} --fasttext-category expenses:food.coffee --yelp-categories cakeshop --out ${output_file} --lower
python3 get_yelp_graphql_data.py --area ${area} --fasttext-category expenses:food.groceries --yelp-categories convenience --out ${output_file} --lower
python3 get_yelp_graphql_data.py --area ${area} --fasttext-category expenses:food.groceries --yelp-categories grocery --out ${output_file} --lower
python3 get_yelp_graphql_data.py --area ${area} --fasttext-category expenses:food.groceries --yelp-categories gourmet --out ${output_file} --lower
python3 get_yelp_graphql_data.py --area ${area} --fasttext-category expenses:food.groceries --yelp-categories intlgrocery --out ${output_file} --lower
python3 get_yelp_graphql_data.py --area ${area} --fasttext-category expenses:food.alcohol-tobacco --yelp-categories tobaccoshops --out ${output_file} --lower
python3 get_yelp_graphql_data.py --area ${area} --fasttext-category expenses:food.bars --yelp-categories bars --out ${output_file} --lower

# HOME
python3 get_yelp_graphql_data.py --area ${area} --fasttext-category expenses:home.mortgage --yelp-categories mortgagebrokers --out ${output_file} --lower
python3 get_yelp_graphql_data.py --area ${area} --fasttext-category expenses:home.rent --yelp-categories apartments --out ${output_file} --lower
python3 get_yelp_graphql_data.py --area ${area} --fasttext-category expenses:home.incurences-fees --yelp-categories insurance --out ${output_file} --lower
python3 get_yelp_graphql_data.py --area ${area} --fasttext-category expenses:home.services --yelp-categories homeservices --out ${output_file} --lower
python3 get_yelp_graphql_data.py --area ${area} --fasttext-category expenses:home.services --yelp-categories locksmiths --out ${output_file} --lower
python3 get_yelp_graphql_data.py --area ${area} --fasttext-category expenses:home.services --yelp-categories painters --out ${output_file} --lower
python3 get_yelp_graphql_data.py --area ${area} --fasttext-category expenses:home.communications --yelp-categories telecommunications --out ${output_file} --lower
python3 get_yelp_graphql_data.py --area ${area} --fasttext-category expenses:home.communications --yelp-categories isps --out ${output_file} --lower
python3 get_yelp_graphql_data.py --area ${area} --fasttext-category expenses:home.communications --yelp-categories televisionserviceproviders --out ${output_file} --lower

# HOUSE
python3 get_yelp_graphql_data.py --area ${area} --fasttext-category expenses:house.garden --yelp-categories gardening --out ${output_file} --lower
python3 get_yelp_graphql_data.py --area ${area} --fasttext-category expenses:house.repairs --yelp-categories handyman --out ${output_file} --lower
python3 get_yelp_graphql_data.py --area ${area} --fasttext-category expenses:house.repairs --yelp-categories electricians --out ${output_file} --lower

# WELLNESS
python3 get_yelp_graphql_data.py --area ${area} --fasttext-category expenses:wellness.healthcare --yelp-categories chiropractors --out ${output_file} --lower
python3 get_yelp_graphql_data.py --area ${area} --fasttext-category expenses:wellness.healthcare --yelp-categories urgent_care --out ${output_file} --lower
python3 get_yelp_graphql_data.py --area ${area} --fasttext-category expenses:wellness.healthcare --yelp-categories physicians --out ${output_file} --lower
python3 get_yelp_graphql_data.py --area ${area} --fasttext-category expenses:wellness.healthcare --yelp-categories rehabilitation_center --out ${output_file} --lower
python3 get_yelp_graphql_data.py --area ${area} --fasttext-category expenses:wellness.healthcare --yelp-categories dentists --out ${output_file} --lower
python3 get_yelp_graphql_data.py --area ${area} --fasttext-category expenses:wellness.healthcare --yelp-categories c_and_mh --out ${output_file} --lower
python3 get_yelp_graphql_data.py --area ${area} --fasttext-category expenses:wellness.healthcare --yelp-categories medcenters --out ${output_file} --lower
python3 get_yelp_graphql_data.py --area ${area} --fasttext-category expenses:wellness.beauty --yelp-categories beautysvc --out ${output_file} --lower
python3 get_yelp_graphql_data.py --area ${area} --fasttext-category expenses:wellness.pharmacy --yelp-categories pharmacy --out ${output_file} --lower
python3 get_yelp_graphql_data.py --area ${area} --fasttext-category expenses:wellness.eyecare --yelp-categories opticians --out ${output_file} --lower

# TRANSPORT
python3 get_yelp_graphql_data.py --area ${area} --fasttext-category expenses:transport.car --yelp-categories auto --out ${output_file} --lower
python3 get_yelp_graphql_data.py --area ${area} --fasttext-category expenses:transport.publictransport --yelp-categories publictransport --out ${output_file} --lower

# SHOPPING
python3 get_yelp_graphql_data.py --area ${area} --fasttext-category expenses:shopping.clothes --yelp-categories fashion --out ${output_file} --lower
python3 get_yelp_graphql_data.py --area ${area} --fasttext-category expenses:shopping.electronics --yelp-categories electronics --out ${output_file} --lower
python3 get_yelp_graphql_data.py --area ${area} --fasttext-category expenses:shopping.books --yelp-categories bookstores --out ${output_file} --lower
python3 get_yelp_graphql_data.py --area ${area} --fasttext-category expenses:shopping.gifts --yelp-categories giftshops --out ${output_file} --lower
python3 get_yelp_graphql_data.py --area ${area} --fasttext-category expenses:shopping.hobby --yelp-categories hobbyshops --out ${output_file} --lower

# ENTERTAINMENT
python3 get_yelp_graphql_data.py --area ${area} --fasttext-category expenses:entertainment.sport --yelp-categories active --out ${output_file} --lower
python3 get_yelp_graphql_data.py --area ${area} --fasttext-category expenses:entertainment.culture --yelp-categories movietheaters --out ${output_file} --lower
python3 get_yelp_graphql_data.py --area ${area} --fasttext-category expenses:entertainment.culture --yelp-categories museums --out ${output_file} --lower
python3 get_yelp_graphql_data.py --area ${area} --fasttext-category expenses:entertainment.culture --yelp-categories arcades --out ${output_file} --lower
python3 get_yelp_graphql_data.py --area ${area} --fasttext-category expenses:entertainment.culture --yelp-categories social_clubs --out ${output_file} --lower
python3 get_yelp_graphql_data.py --area ${area} --fasttext-category expenses:entertainment.culture --yelp-categories culturalcenter --out ${output_file} --lower
