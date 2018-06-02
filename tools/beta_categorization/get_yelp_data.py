from bs4 import BeautifulSoup
from argparse import ArgumentParser
import requests
import logging
import sys

logging.basicConfig(level=logging.INFO, format="%(asctime)s %(levelname)s %(message)s")

def fetch_from_yelp(yelp_category, fasttext_category, country, file_name, lowercase):
    url = "https://www.yelp.com/search?cflt={}&find_loc={}&start={}"
    data = [];
    logging.info("Fetching data...")
    for i in range(0, 110, 10):
        page = requests.get(url.format(yelp_category, country, i))
        soup = BeautifulSoup(page.content, 'html.parser')
        names = soup.find_all("a", {"class": "biz-name"})
        for name in names:
            if lowercase:
                data.append(name.span.text.lower())
            else:
                data.append(name.span.text)
    logging.info("Writing to output file...")
    with open(file_name, "a+") as output_file:
        for datapoint in data:
            output_file.write("__label__{} {}\n".format(fasttext_category, datapoint))
    logging.info("Done writing to file: " + file_name)

def main():
    parser = ArgumentParser(description="Fetch data from Yelp ")

    parser.add_argument("--out", metavar="output.txt", type=str, help="the output file", default="yelp_output.txt")
    parser.add_argument("--country", metavar="Sweden", type=str, help="Country name in ISO-2166 format", required=True)
    parser.add_argument("--fasttext-category", metavar="expenses:food.restaurants", help="the FastText category to use", required=True)
    parser.add_argument("--lowercase", action="store_true", help="transform data to lowercase", default=False)
    parser.add_argument("--yelp-category", metavar="homeservices", help="Yelp category to search for, check https://www.yelp.com/developers/documentation/v3/category_list and select the appropriate country", required=True)

    args = parser.parse_args()

    logging.info("Program arguments: " + str(args))

    fetch_from_yelp(args.yelp_category, args.fasttext_category, args.country, args.out, args.lowercase)

if __name__ == "__main__":
    sys.exit(main())