import operator
import sys
import logging
from argparse import ArgumentParser

logging.basicConfig(level=logging.INFO, format="%(asctime)s %(levelname)s %(message)s")


def read_transactions(file):
    with open(file, 'r') as f:
        transactions = f.readlines()
    return transactions


def find_keywords(transactions, args):
    word_counts = {}
    unique_descriptions = set()
    words_to_category_distribution = {}
    nr_of_results = 0
    output = "KEYWORDS\n\n"
    fasttext_output = ""

    income = args.income
    lower = args.lower
    unique = args.unique
    min_word_length = args.min
    number_of_keywords = args.keywords
    number_of_categories_per_word = args.categories

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

        if description not in unique_descriptions:
            unique_descriptions.add(description)
        else:
            if unique:
                continue

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

            if word not in words_to_category_distribution:
                words_to_category_distribution[word] = {}
            else:
                category_count = words_to_category_distribution[word]
                if category not in category_count:
                    category_count[category] = 1
                else:
                    category_count[category] = category_count[category] + 1

    logging.info("Nr of results: " + str(nr_of_results))

    keywords_sorted = sorted(word_counts.items(), key=operator.itemgetter(1), reverse=True)

    for i in range(number_of_keywords):
        if i == len(keywords_sorted):
            break

        keyword = keywords_sorted[i][0]
        count = keywords_sorted[i][1]

        output += keyword + " " + str(count) + "\n"
        category_distribution = words_to_category_distribution[keyword]
        top_categories = sorted(category_distribution.items(), key=operator.itemgetter(1), reverse=True)[
                         0:number_of_categories_per_word]
        for category_and_count in top_categories:
            output += (str(category_and_count[0]) + " (")
            output += (str(round(category_and_count[1] / count * 100, 1)) + "%)\n")
        output += "\n"

        fasttext_output += "__label__{} {}\n".format(top_categories[0][0], keyword)

    return output, fasttext_output


def main():
    parser = ArgumentParser(description="Get keyword counts from a file and corresponding categories.")

    parser.add_argument("--file", metavar="file.txt", type=str, help="the input file", required=True)
    parser.add_argument("--out", metavar="keywords.txt", type=str, help="the output file", default="keywords.txt")
    parser.add_argument("--keywords", metavar="20", type=int, help="number of keywords to produce", default=20)
    parser.add_argument("--min", metavar="2", type=int, help="minimum length of a word", default=2)
    parser.add_argument("--categories", metavar="5", type=int, help="number of categories to show per word", default=5)
    parser.add_argument("--fasttext", action="store_true", help="enables FastText output in the output file, picking "
                                                                "the top category for each word")
    parser.add_argument("--income", action="store_true",
                        help="if set, only deals with income and if not, only with expenses")
    parser.add_argument("--unique", action="store_true",
                        help="if set, keywords are extracted from unique description data")

    args = parser.parse_args()
    logging.info("Program arguments: " + str(args))

    logging.info("Reading transactions...")
    transactions = read_transactions(args.file)
    logging.info("Done reading transactions.")

    logging.info("Finding keywords...")
    output, fasttext_output = find_keywords(transactions, args)
    logging.info("Done finding keywords.")

    logging.info("Writing to output file...")
    output_file = open(args.out, "w+")
    output_file.write(fasttext_output) if args.fasttext else output_file.write(output)
    logging.info("Done writing to file: " + args.out)

    return 0


if __name__ == "__main__":
    sys.exit(main())
