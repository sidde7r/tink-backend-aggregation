import sys
import logging
import glob
from argparse import ArgumentParser

logging.basicConfig(level=logging.INFO, format="%(asctime)s %(levelname)s %(message)s")

def main():
    parser = ArgumentParser(description="Combines global and country specific files into one training file")

    parser.add_argument("--out", metavar="output.txt", type=str, help="the output file")
    parser.add_argument("--global_folder", metavar="global_data", type=str, help="folder containing global data", default="global_data")
    parser.add_argument("--country_folder", metavar="", type=str, help="folder containing local data")

    args = parser.parse_args()
    logging.info("Program arguments: " + str(args))

    global_files = glob.glob(args.global_folder + '/*')
    if args.country_folder:
        country_files = glob.glob(args.country_folder + '/*')
    else:
        country_files = []

    files = country_files + global_files

    output = ''
    for file in files:
        f = open(file, 'r')
        output = output + ''.join(f.readlines())
        f.close()


    if args.out:
        f = open(args.out, 'w+')
        f.write(output)
        f.close()
    else:
        print(output)

    return 0

if __name__ == "__main__":
    sys.exit(main())
