#!/bin/bash

# This script fetches data from Open Street Map (OSM) via another script. The OSM categories have here been mapped to the Oxford categories.
# The output is in a FastText format (e.g. "__label__expenses:food.restaurants Pizza Hut") which later can be used to append to the training
# set of a categorization model.

# For an overview of possible OSM tags/categories, check out: http://wiki.openstreetmap.org/wiki/Map_Features.

timestamp=$(date +%s)
area=$1
output_file=osm_output_${area}_${timestamp}.txt

fetch_OSM_data()
{
  # FOOD
  python3 get_osm_data.py --area ${area} --key amenity --value bar --append --fasttext --category expenses:food.bars --out ${output_file}
  python3 get_osm_data.py --area ${area} --key amenity --value pub --append --fasttext --category expenses:food.bars --out ${output_file}
  python3 get_osm_data.py --area ${area} --key amenity --value nightclub --append --fasttext --category expenses:food.bars --out ${output_file}

  python3 get_osm_data.py --area ${area} --key amenity --value restaurant --append --fasttext --category expenses:food.restaurants --out ${output_file}
  python3 get_osm_data.py --area ${area} --key amenity --value food_court --append --fasttext --category expenses:food.restaurants --out ${output_file}
  python3 get_osm_data.py --area ${area} --key amenity --value fast_food --append --fasttext --category expenses:food.restaurants --out ${output_file}

  python3 get_osm_data.py --area ${area} --key amenity --value cafe --append --fasttext --category expenses:food.coffee --out ${output_file}
  python3 get_osm_data.py --area ${area} --key amenity --value ice_cream --append --fasttext --category expenses:food.coffee --out ${output_file}
  python3 get_osm_data.py --area ${area} --key shop --value ice_cream --append --fasttext --category expenses:food.coffee --out ${output_file}
  python3 get_osm_data.py --area ${area} --key shop --value bakery --append --fasttext --category expenses:food.coffee --out ${output_file}
  python3 get_osm_data.py --area ${area} --key shop --value coffee --append --fasttext --category expenses:food.coffee --out ${output_file}
  python3 get_osm_data.py --area ${area} --key shop --value chocolate --append --fasttext --category expenses:food.coffee --out ${output_file}
  python3 get_osm_data.py --area ${area} --key shop --value confectionery --append --fasttext --category expenses:food.coffee --out ${output_file}
  python3 get_osm_data.py --area ${area} --key shop --value pastry --append --fasttext --category expenses:food.coffee --out ${output_file}

  python3 get_osm_data.py --area ${area} --key shop --value butcher --append --fasttext --category expenses:food.groceries --out ${output_file}
  python3 get_osm_data.py --area ${area} --key shop --value cheese --append --fasttext --category expenses:food.groceries --out ${output_file}
  python3 get_osm_data.py --area ${area} --key shop --value convenience --append --fasttext --category expenses:food.groceries --out ${output_file}
  python3 get_osm_data.py --area ${area} --key shop --value supermarket --append --fasttext --category expenses:food.groceries --out ${output_file}
  python3 get_osm_data.py --area ${area} --key shop --value dairy --append --fasttext --category expenses:food.groceries --out ${output_file}
  python3 get_osm_data.py --area ${area} --key shop --value farm --append --fasttext --category expenses:food.groceries --out ${output_file}

  python3 get_osm_data.py --area ${area} --key shop --value alcohol --append --fasttext --category expenses:food.alcohol-tobacco --out ${output_file}
  python3 get_osm_data.py --area ${area} --key shop --value wine --append --fasttext --category expenses:food.alcohol-tobacco --out ${output_file}
  python3 get_osm_data.py --area ${area} --key shop --value tobacco --append --fasttext --category expenses:food.alcohol-tobacco --out ${output_file}

  # WELLNESS
  python3 get_osm_data.py --area ${area} --key shop --value perfumery --append --fasttext --category expenses:wellness.other --out ${output_file}
  python3 get_osm_data.py --area ${area} --key shop --value nutrition_supplements --append --fasttext --category expenses:wellness.other --out ${output_file}
  python3 get_osm_data.py --area ${area} --key shop --value herbalist --append --fasttext --category expenses:wellness.other --out ${output_file}
  python3 get_osm_data.py --area ${area} --key shop --value massage --append --fasttext --category expenses:wellness.other --out ${output_file}
  python3 get_osm_data.py --area ${area} --key shop --value tattoo --append --fasttext --category expenses:wellness.other --out ${output_file}
  python3 get_osm_data.py --area ${area} --key shop --value tattoo --append --fasttext --category expenses:wellness.other --out ${output_file}

  python3 get_osm_data.py --area ${area} --key amenity --value doctors --append --fasttext --category expenses:wellness.healthcare --out ${output_file}
  python3 get_osm_data.py --area ${area} --key amenity --value dentist --append --fasttext --category expenses:wellness.healthcare --out ${output_file}
  python3 get_osm_data.py --area ${area} --key amenity --value hospital --append --fasttext --category expenses:wellness.healthcare --out ${output_file}
  python3 get_osm_data.py --area ${area} --key amenity --value clinic --append --fasttext --category expenses:wellness.healthcare --out ${output_file}

  python3 get_osm_data.py --area ${area} --key amenity --value pharmacy --append --fasttext --category expenses:wellness.pharmacy --out ${output_file}
  python3 get_osm_data.py --area ${area} --key shop --value medical_supply --append --fasttext --category expenses:wellness.pharmacy --out ${output_file}


  python3 get_osm_data.py --area ${area} --key shop --value hairdresser --append --fasttext --category expenses:wellness.beauty --out ${output_file}
  python3 get_osm_data.py --area ${area} --key shop --value cosmetics --append --fasttext --category expenses:wellness.beauty --out ${output_file}
  python3 get_osm_data.py --area ${area} --key shop --value beauty --append --fasttext --category expenses:wellness.beauty --out ${output_file}

  python3 get_osm_data.py --area ${area} --key shop --value optician --append --fasttext --category expenses:wellness.eyecare --out ${output_file}

  # ENTERTAINMENT
  python3 get_osm_data.py --area ${area} --key shop --value travel_agency --append --fasttext --category expenses:entertainment.vacation --out ${output_file}
  python3 get_osm_data.py --area ${area} --key tourism --value hostel --append --fasttext --category expenses:entertainment.vacation --out ${output_file}
  python3 get_osm_data.py --area ${area} --key tourism --value hotel --append --fasttext --category expenses:entertainment.vacation --out ${output_file}
  python3 get_osm_data.py --area ${area} --key tourism --value motel --append --fasttext --category expenses:entertainment.vacation --out ${output_file}
  python3 get_osm_data.py --area ${area} --key tourism --value camp_site --append --fasttext --category expenses:entertainment.vacation --out ${output_file}
  python3 get_osm_data.py --area ${area} --key tourism --value guest_house --append --fasttext --category expenses:entertainment.vacation --out ${output_file}

  python3 get_osm_data.py --area ${area} --key shop --value collector --append --fasttext --category expenses:entertainment.hobby --out ${output_file}
  python3 get_osm_data.py --area ${area} --key shop --value craft --append --fasttext --category expenses:entertainment.hobby --out ${output_file}
  python3 get_osm_data.py --area ${area} --key shop --value frame --append --fasttext --category expenses:entertainment.hobby --out ${output_file}
  python3 get_osm_data.py --area ${area} --key shop --value music --append --fasttext --category expenses:entertainment.hobby --out ${output_file}
  python3 get_osm_data.py --area ${area} --key shop --value musical_instrument --append --fasttext --category expenses:entertainment.hobby --out ${output_file}

  python3 get_osm_data.py --area ${area} --key amenity --value cinema --append --fasttext --category expenses:entertainment.culture --out ${output_file}
  python3 get_osm_data.py --area ${area} --key amenity --value theatre --append --fasttext --category expenses:entertainment.culture --out ${output_file}
  python3 get_osm_data.py --area ${area} --key tourism --value museum --append --fasttext --category expenses:entertainment.culture --out ${output_file}
  python3 get_osm_data.py --area ${area} --key tourism --value theme_park --append --fasttext --category expenses:entertainment.culture --out ${output_file}
  python3 get_osm_data.py --area ${area} --key tourism --value zoo --append --fasttext --category expenses:entertainment.culture --out ${output_file}

  python3 get_osm_data.py --area ${area} --key amenity --value gambling --append --fasttext --category expenses:entertainment.other --out ${output_file}
  python3 get_osm_data.py --area ${area} --key amenity --value casino --append --fasttext --category expenses:entertainment.other --out ${output_file}
  python3 get_osm_data.py --area ${area} --key leisure --value amusement_arcade --append --fasttext --category expenses:entertainment.other --out ${output_file}

  python3 get_osm_data.py --area ${area} --key amenity --value gym --append --fasttext --category expenses:entertainment.sport --out ${output_file}
  python3 get_osm_data.py --area ${area} --key leisure --value fitness_centre --append --fasttext --category expenses:entertainment.sport --out ${output_file}

  # SHOPPING
  python3 get_osm_data.py --area ${area} --key shop --value clothes --append --fasttext --category expenses:shopping.clothes --out ${output_file}
  python3 get_osm_data.py --area ${area} --key shop --value bags --append --fasttext --category expenses:shopping.clothes --out ${output_file}
  python3 get_osm_data.py --area ${area} --key shop --value fashion --append --fasttext --category expenses:shopping.clothes --out ${output_file}
  python3 get_osm_data.py --area ${area} --key shop --value jewelry --append --fasttext --category expenses:shopping.clothes --out ${output_file}
  python3 get_osm_data.py --area ${area} --key shop --value shoes --append --fasttext --category expenses:shopping.clothes --out ${output_file}
  python3 get_osm_data.py --area ${area} --key shop --value tailor --append --fasttext --category expenses:shopping.clothes --out ${output_file}
  python3 get_osm_data.py --area ${area} --key shop --value watches --append --fasttext --category expenses:shopping.clothes --out ${output_file}
  python3 get_osm_data.py --area ${area} --key shop --value second_hand --append --fasttext --category expenses:shopping.clothes --out ${output_file}

  python3 get_osm_data.py --area ${area} --key shop --value electrical --append --fasttext --category expenses:shopping.electronics --out ${output_file}
  python3 get_osm_data.py --area ${area} --key shop --value computer --append --fasttext --category expenses:shopping.electronics --out ${output_file}
  python3 get_osm_data.py --area ${area} --key shop --value robot --append --fasttext --category expenses:shopping.electronics --out ${output_file}
  python3 get_osm_data.py --area ${area} --key shop --value electronics --append --fasttext --category expenses:shopping.electronics --out ${output_file}
  python3 get_osm_data.py --area ${area} --key shop --value hifi --append --fasttext --category expenses:shopping.electronics --out ${output_file}
  python3 get_osm_data.py --area ${area} --key shop --value mobile_phone --append --fasttext --category expenses:shopping.electronics --out ${output_file}
  python3 get_osm_data.py --area ${area} --key shop --value radiotechnics --append --fasttext --category expenses:shopping.electronics --out ${output_file}
  python3 get_osm_data.py --area ${area} --key shop --value vacuum_cleaner --append --fasttext --category expenses:shopping.electronics --out ${output_file}
  python3 get_osm_data.py --area ${area} --key shop --value camera --append --fasttext --category expenses:shopping.electronics --out ${output_file}

  python3 get_osm_data.py --area ${area} --key shop --value fishing --append --fasttext --category expenses:shopping.hobby --out ${output_file}
  python3 get_osm_data.py --area ${area} --key shop --value hunting --append --fasttext --category expenses:shopping.hobby --out ${output_file}
  python3 get_osm_data.py --area ${area} --key shop --value bicycle --append --fasttext --category expenses:shopping.hobby --out ${output_file}
  python3 get_osm_data.py --area ${area} --key shop --value outdoor --append --fasttext --category expenses:shopping.hobby --out ${output_file}
  python3 get_osm_data.py --area ${area} --key shop --value ski --append --fasttext --category expenses:shopping.hobby --out ${output_file}
  python3 get_osm_data.py --area ${area} --key shop --value sports --append --fasttext --category expenses:shopping.hobby --out ${output_file}
  python3 get_osm_data.py --area ${area} --key shop --value swimming_pool --append --fasttext --category expenses:shopping.hobby --out ${output_file}

  python3 get_osm_data.py --area ${area} --key shop --value games --append --fasttext --category expenses:shopping.books --out ${output_file}
  python3 get_osm_data.py --area ${area} --key shop --value video_games --append --fasttext --category expenses:shopping.books --out ${output_file}
  python3 get_osm_data.py --area ${area} --key shop --value books --append --fasttext --category expenses:shopping.books --out ${output_file}
  python3 get_osm_data.py --area ${area} --key amenity --value library --append --fasttext --category expenses:shopping.books --out ${output_file}

  # HOUSE
  python3 get_osm_data.py --area ${area} --key shop --value bathroom_furnishing --append --fasttext --category expenses:house.fitment --out ${output_file}
  python3 get_osm_data.py --area ${area} --key shop --value fireplace --append --fasttext --category expenses:house.fitment --out ${output_file}
  python3 get_osm_data.py --area ${area} --key shop --value houseware --append --fasttext --category expenses:house.fitment --out ${output_file}
  python3 get_osm_data.py --area ${area} --key shop --value antiques --append --fasttext --category expenses:house.fitment --out ${output_file}
  python3 get_osm_data.py --area ${area} --key shop --value bed --append --fasttext --category expenses:house.fitment --out ${output_file}
  python3 get_osm_data.py --area ${area} --key shop --value candles --append --fasttext --category expenses:house.fitment --out ${output_file}
  python3 get_osm_data.py --area ${area} --key shop --value carpet --append --fasttext --category expenses:house.fitment --out ${output_file}
  python3 get_osm_data.py --area ${area} --key shop --value curtain --append --fasttext --category expenses:house.fitment --out ${output_file}
  python3 get_osm_data.py --area ${area} --key shop --value furniture --append --fasttext --category expenses:house.fitment --out ${output_file}
  python3 get_osm_data.py --area ${area} --key shop --value interior_decoration --append --fasttext --category expenses:house.fitment --out ${output_file}
  python3 get_osm_data.py --area ${area} --key shop --value lamps --append --fasttext --category expenses:house.fitment --out ${output_file}
  python3 get_osm_data.py --area ${area} --key shop --value art --append --fasttext --category expenses:house.fitment --out ${output_file}

  python3 get_osm_data.py --area ${area} --key shop --value florist --append --fasttext --category expenses:house.garden --out ${output_file}
  python3 get_osm_data.py --area ${area} --key shop --value garden_centre --append --fasttext --category expenses:house.garden --out ${output_file}

  python3 get_osm_data.py --area ${area} --key shop --value garden_furniture --append --fasttext --category expenses:house.other --out ${output_file}

  python3 get_osm_data.py --area ${area} --key shop --value glaziery --append --fasttext --category expenses:house.repairs --out ${output_file}
  python3 get_osm_data.py --area ${area} --key shop --value hardware --append --fasttext --category expenses:house.repairs --out ${output_file}
  python3 get_osm_data.py --area ${area} --key shop --value paint --append --fasttext --category expenses:house.repairs --out ${output_file}
  python3 get_osm_data.py --area ${area} --key shop --value doityourself --append --fasttext --category expenses:house.repairs --out ${output_file}
  python3 get_osm_data.py --area ${area} --key shop --value doors --append --fasttext --category expenses:house.repairs --out ${output_file}
  python3 get_osm_data.py --area ${area} --key shop --value flooring --append --fasttext --category expenses:house.repairs --out ${output_file}
  python3 get_osm_data.py --area ${area} --key shop --value tiles --append --fasttext --category expenses:house.repairs --out ${output_file}
  python3 get_osm_data.py --area ${area} --key shop --value kitchen --append --fasttext --category expenses:house.repairs --out ${output_file}
  python3 get_osm_data.py --area ${area} --key shop --value window_blind --append --fasttext --category expenses:house.repairs --out ${output_file}

  # TRANSPORT
  python3 get_osm_data.py --area ${area} --key shop --value car --append --fasttext --category expenses:transport.car --out ${output_file}
  python3 get_osm_data.py --area ${area} --key shop --value car_repair --append --fasttext --category expenses:transport.car --out ${output_file}
  python3 get_osm_data.py --area ${area} --key shop --value car_parts --append --fasttext --category expenses:transport.car --out ${output_file}
  python3 get_osm_data.py --area ${area} --key amenity --value fuel --append --fasttext --category expenses:transport.car --out ${output_file}

  # HOME
  python3 get_osm_data.py --area ${area} --key shop --value dry_cleaning --append --fasttext --category expenses:home.services --out ${output_file}
  python3 get_osm_data.py --area ${area} --key shop --value laundry --append --fasttext --category expenses:home.services --out ${output_file}

  # MISCELLANEOUS
  python3 get_osm_data.py --area ${area} --key shop --value toys --append --fasttext --category expenses:misc.kids --out ${output_file}
}

if [ "$1" != "" ]; then
    fetch_OSM_data
else
  echo "Please provide area code"
fi