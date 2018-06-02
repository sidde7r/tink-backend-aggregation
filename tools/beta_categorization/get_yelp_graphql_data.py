from argparse import ArgumentParser
import requests
import logging
import sys
import os
import json

logging.basicConfig(level=logging.INFO, format="%(asctime)s %(levelname)s %(message)s")

OSM_URL = "http://overpass-api.de/api/interpreter?data=[out:json];" \
          "area[\"ISO3166-1\"=\"{}\"];" \
          "(node[place=\"city\"](area););" \
          "out;"

YELP_LIMIT = 500
YELP_API_TOKEN = os.environ.get("YELP_API_TOKEN")
YELP_URL = "https://api.yelp.com/v3/graphql"
YELP_HEADER = {"Authorization": "Bearer {}".format(YELP_API_TOKEN), "Content-Type": "application/json"}
YEALP_SEARCH_QUERY = """
{{
    search(categories:  "{}",
            location: "{}",
            limit: {},
            offset: {}) {{
        total
        business {{
            name
        }}
    }}
}}"""

def get_cities_from_OSM(area):
    """Will filter out cities with too low population or without population
    data available"""
    response = requests.get(OSM_URL.format(area))
    if response.status_code == 200:
        elements = json.loads(response.content)["elements"]
        cities = []
        min_city_population = 10000
        elements_with_population = [elem for elem in elements if "population" in elem["tags"]
                and int(elem["tags"]["population"]) > min_city_population]
        for elem in elements_with_population:
            cities.append((elem["tags"]["name"], elem["tags"]["population"]))
        cities = sorted(cities, reverse=True,
                        key=lambda population: int(population[1]))

        # Extract the city names only as we don't care about population
        return [list(t) for t in zip(*cities)][0]
    else:
        logging.info("No cities found for area {}".format(area))
        sys.exit()

class FailedSearchException(Exception):
    pass

def get_from_yelp(yelp_categories, city, offset):
    query = {"query": YEALP_SEARCH_QUERY.format(
        yelp_categories, city, YELP_LIMIT, offset)}
    try:
        response = requests.post(YELP_URL, json=query, headers=YELP_HEADER)
        response.raise_for_status()
        response_data = response.json()
        if "errors" in response_data:
            raise FailedSearchException
        businesses = response_data["data"]["search"]["business"]
        total = response_data["data"]["search"]["total"]
        return {
            "businesses": businesses,
            "total": total
        }
    except (requests.exceptions.HTTPError, FailedSearchException) as err:
        logging.error(err)
        return {
            "businesses": [],
            "total": 0
        }

def request_yelp_data(yelp_categories, fasttext_category, cities, file_name, lowercase):
    # Search only the 10 most populated cities
    yelp_data = []
    for city in cities[:10]:
        current_offset = 0
        logging.info("Fetching data from yelp for {} in {}".format(yelp_categories, city))
        while current_offset == 0 or current_offset <= 500:
            partial_data = get_from_yelp(yelp_categories, city, current_offset)
            if len(partial_data["businesses"]) == 0 or current_offset >= partial_data["total"]:
                break
            current_offset += len(partial_data["businesses"])
            for business in partial_data["businesses"]:
                if lowercase:
                    yelp_data.append(business["name"].lower())
                else:
                    yelp_data.append(business["name"])

    logging.info("Writing to output file...")
    for data_point in yelp_data:
        with open(file_name, "a+") as output_file:
            if fasttext_category is not None:
                output_file.write("__label__{} {}\n".format(fasttext_category, data_point))
            else:
                output_file.write(data_point + "\n")
    logging.info("Done writing to file: " + file_name)

def main():
    if YELP_API_TOKEN is None:
        logging.warn("Please set your \"YELP_API_TOKEN\" environment variable")
        sys.exit()

    parser = ArgumentParser(description="Fetch data from Yelp's GraphQL API")

    parser.add_argument("--out", metavar="output.txt", type=str, help="the output file", default="yelp_output.txt")
    parser.add_argument("--area", metavar="SE", type=str, help="area code in ISO3166-1 format", required=True)
    parser.add_argument("--lowercase", action="store_true", help="transform data to lowercase", default=False)
    parser.add_argument("--fasttext-category", metavar="expenses:food.restaurants", help="the FastText category to use")
    parser.add_argument("--yelp-categories", metavar="homeservices", help="Yelp categories to search for, check https://www.yelp.com/developers/documentation/v3/category_list and select the appropriate country", required=True)
    parser.add_argument("--fetch-all", action="store_true", help="Paginate through the results and fetch everything. API is paginated by default", default=True)
    args = parser.parse_args()

    logging.info("Program arguments: " + str(args))
    cities = get_cities_from_OSM(args.area)
    request_yelp_data(args.yelp_categories, args.fasttext_category, cities, args.out, args.lowercase)

if __name__ == "__main__":
    sys.exit(main())
