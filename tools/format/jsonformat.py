#!/usr/bin/env python

import json
import sys


def format_json_file(path):
    with open(path, 'r') as f:
        content = json.load(f)

    with open(path, 'w') as f:
        json.dump(content, f, ensure_ascii=False, indent=4)
        f.write("\n")  # Always newline at EOF


if __name__ == '__main__':

    if len(sys.argv) <= 1:
        sys.exit("Usage: python {} <jsonfile> [<jsonfile> [...]]".format(sys.argv[0]))

    paths = sys.argv[1:]

    for path in paths:
        format_json_file(path)
