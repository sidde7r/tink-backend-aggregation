import requests
import json
from argparse import ArgumentParser
import logging
import sys

logging.basicConfig(level=logging.INFO, format="%(asctime)s %(levelname)s %(message)s")


def get_data_in_area(area, key, value, lower):
    # The target specifies what tag we want to search for. Depending on what we want to fetch, there will be different
    # key/value pairs. Examples are shop:supermarket and amenity:restaurant. If something is listed as an amenity or
    # has its own key has to be figured out by the user of this script.
    # Have a look here for more info: http://wiki.openstreetmap.org/wiki/Map_Features
    target = "[{}={}](area.searchArea);".format(key, value)

    # The OSM has three main types of data: node, way and relation. These can all be tagged with the same type of tags,
    # so we have to search through all of them if we want to find all data with a specific tag.
    request_url = 'http://overpass-api.de/api/interpreter?data=[out:json];' \
                  'area["ISO3166-1"="{}"]->.searchArea;(' \
                  'node{}' \
                  'way{}' \
                  'relation{});' \
                  'out body;'.format(area, target, target, target)

    response = requests.get(request_url)

    # If you do too many requests you'll get an error message. Checking arbitrary length here since if it's much data
    # it's probably not an error.
    if response.status_code == 429:
        logging.error("Did not get any response since the request rate is limited")
    elif response.status_code < 200 or response.status_code >= 300:
        logging.error("Did not get a successful response (code {}): {}".format(response.status_code, response.content))

    elements = json.loads(response.content)["elements"]

    tags_in_area = set()
    for element in elements:
        if "tags" in element and "name" in element["tags"]:
            name = str(element["tags"]["name"]).lower() if lower else element["tags"]["name"]
            tags_in_area.add(name)

    return tags_in_area


def main():
    parser = ArgumentParser(description="Fetch OSM data by tags (key=value) and location.")

    parser.add_argument("--out", metavar="output.txt", type=str, help="the output file", default="output.txt")
    parser.add_argument("--area", metavar="SE", type=str, help="area code in ISO3166-1 format", required=True)
    parser.add_argument("--key", metavar="amenity", type=str, help="the key to search for", required=True)
    parser.add_argument("--value", metavar="restaurant", type=str, help="the value of the key", required=True)
    parser.add_argument("--fasttext", action="store_true", help="enables FastText output in the output file")
    parser.add_argument("--lower", action="store_true", help="transforms the names to lowercase")
    parser.add_argument("--category", metavar="expenses:food.restaurants", help="the FastText category to use")
    parser.add_argument("--append", action="store_true", help="appends to the output file, so multiple calls of "
                                                              "this script can be done on the same file")

    args = parser.parse_args()
    if args.fasttext and not args.category:
        logging.error("You must define a category for FastText")
        return 1

    logging.info("Program arguments: " + str(args))

    logging.info("Fetching data...")
    all_data = get_data_in_area(args.area, args.key, args.value, args.lower)
    logging.info("Done fetching data.")

    logging.info("Found {} unique data points with tags {}={} in area '{}'.".format(str(len(all_data)), args.key,
                                                                                    args.value, args.area))

    logging.info("Writing to output file...")
    output_file = open(args.out, "a+") if args.append else open(args.out, "w+")
    for data_point in all_data:
        if args.fasttext:
            output_file.write("__label__{} {}\n".format(args.category, data_point))
        else:
            output_file.write(data_point + "\n")

    output_file.close()
    logging.info("Done writing to file: " + args.out)

    return 0


if __name__ == "__main__":
    sys.exit(main())
