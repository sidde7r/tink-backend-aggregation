import csv
import operator
import sys
import logging
from argparse import ArgumentParser

logging.basicConfig(level=logging.INFO, format="%(asctime)s %(levelname)s %(message)s")


# This script can be used to get a quick overview of the data and see what words are most common. It does not take
# into account which category a word is often associated with (there's another script for that).

def read_transactions(file):
    with open(file, 'r') as f:
        transactions = f.readlines()
    return transactions


def find_keywords(transactions, number_of_keywords, min_word_length, income):
    word_counts = {}
    nr_of_results = 0
    output = "MOST COMMON WORDS WITHIN NON-UNIQUE DESCRIPTIONS\n\n"

    for transaction in transactions:
        fasttext_label_prefix = "__label__"
        if fasttext_label_prefix not in transaction:
            logging.error("The input must be in fasttext format")
            sys.exit(1)

        transaction = transaction.replace(fasttext_label_prefix, "").replace("\n", "")
        transaction = transaction.split(" ", 1)
        category = transaction[0]
        description = transaction[1]

        description = str.lower(description)

        if income:
            if "expenses" in category or "transfers" in category:
                continue
        else:
            if "income" in category:
                continue

        nr_of_results += 1
        words = description.split(" ")

        for word in words:
            if len(word) < min_word_length:
                continue

            if word not in word_counts:
                word_counts[word] = 1
            else:
                word_counts[word] = word_counts[word] + 1

    logging.info("Nr of results: " + str(nr_of_results))

    keywords_sorted = sorted(word_counts.items(), key=operator.itemgetter(1), reverse=True)

    for i in range(number_of_keywords):
        if i == len(keywords_sorted):
            break

        keyword = keywords_sorted[i][0]
        count = keywords_sorted[i][1]

        output += keyword + " " + str(count) + "\n"

    return output


def main():
    parser = ArgumentParser(description="Get keyword counts from a file and corresponding categories.")

    parser.add_argument("--file", metavar="file.txt", type=str, help="the input file", required=True)
    parser.add_argument("--out", metavar="keywords.txt", type=str, help="the output file", default="keywords.txt")
    parser.add_argument("--keywords", metavar="20", type=int, help="number of keywords to produce", default=20)
    parser.add_argument("--min", metavar="2", type=int, help="minimum length of a word", default=2)
    parser.add_argument("--income", action="store_true",
                        help="if set, only deals with income and if not, only with expenses")

    args = parser.parse_args()
    logging.info("Program arguments: " + str(args))

    logging.info("Reading transactions...")
    transactions = read_transactions(args.file)
    logging.info("Done reading transactions.")

    logging.info("Finding keywords...")
    output = find_keywords(transactions, args.keywords, args.min, args.income)
    logging.info("Done finding keywords.")

    logging.info("Writing to output file...")
    output_file = open(args.out, "w+")
    output_file.write(output)
    logging.info("Done writing to file: " + args.out)

    return 0


if __name__ == "__main__":
    sys.exit(main())
