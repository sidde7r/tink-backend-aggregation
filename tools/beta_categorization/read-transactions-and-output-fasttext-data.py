import csv
import sys
from argparse import ArgumentParser
import logging

logging.basicConfig(level=logging.INFO, format="%(asctime)s %(levelname)s %(message)s")


def read_transactions(lower, unique, income, input_file):
    unique_trx = {}
    unique_categories = {}
    count_all = 0
    fasttext_output = ""

    with open(input_file, 'r') as f:
        reader = csv.reader(f)
        data_columns = f.readline().replace("\n", "").split(",")
        transactions = list(reader)

    for line in transactions:
        category = str(line[data_columns.index("code")])
        description = line[data_columns.index("description")]
        if lower:
            description = str.lower(description)

        if category not in unique_categories:
            unique_categories[category] = 1
        else:
            unique_categories[category] = unique_categories[category] + 1

        if income:
            if "expenses" in category or "transfers" in category:
                continue
        else:
            if "income" in category:
                continue

        # Example: __label__expenses:shopping.clothes H&M drottninggatan
        output_line = "__label__" + category + " " + description

        if description not in unique_trx:
            unique_trx[description] = 1
            already_in_file = False
        else:
            unique_trx[description] = unique_trx[description] + 1
            already_in_file = True
        count_all += 1

        if (unique and not already_in_file) or (not unique):
            fasttext_output += (output_line + "\n")

    logging.info("Unique: " + str(len(unique_trx)))
    logging.info("Total count: " + str(count_all))
    category_to_percentage = {}

    for category, count in unique_categories.items():
        percentage = float(count / count_all) * 100
        category_to_percentage[category] = percentage

    return fasttext_output


def get_suffix(lower, unique, income):
    suffix = ""

    if income:
        suffix += "_income"

    if lower:
        suffix += "_lower"

    if unique:
        suffix += "_unique"

    return suffix


def main():
    parser = ArgumentParser(description="Takes a CSV input file with transactions and outputs them in FastText format.")

    parser.add_argument("--path", metavar="/Users/myuser/data", type=str,
                        help="the path where the data output file should go", required=True)
    parser.add_argument("--input", metavar="/Users/myuser/data/input.txt", type=str,
                        help="the input file with transaction data", required=True)
    parser.add_argument("--area", metavar="se", type=str,
                        help="the area code which is only used for prefixing the output file with", required=True)
    parser.add_argument("--income", action="store_true",
                        help="if set, only deals with income and if not, only with expenses")
    parser.add_argument("--unique", action="store_true",
                        help="if set, keywords are extracted from unique description data")
    parser.add_argument("--lower", action="store_true", help="if set, transforms data to lowercase")

    args = parser.parse_args()
    logging.info("Program arguments: " + str(args))

    logging.info("Reading transactions...")
    fasttext_output = read_transactions(args.lower, args.unique, args.income, args.input)
    logging.info("Done reading transactions.")

    logging.info("Writing to output file...")
    filename = args.path + "/" + args.area + "_test_set" + get_suffix(args.lower, args.unique, args.income) + ".txt"
    logging.info("Writing to file: " + filename)
    test_set_output = open(filename, "w+")
    test_set_output.write(fasttext_output)
    logging.info("Done writing to file")

    return 0


if __name__ == "__main__":
    sys.exit(main())
