#!/usr/bin/env python3
import hashlib
from zipfile import ZIP_STORED, ZipFile, ZipInfo

DUPE = object()
ZIP_EPOC = (1980, 1, 1, 0, 0, 0)


def main(argv=None):
    name, outfile, *infiles = list(argv) + ["this", ""]
    paths = {}
    files = {}

    for filename, content in file_source(infiles):
        if maybe_save(files, paths, filename, content) == DUPE:
            print("Conflicting versions of " + filename + " found.")
            return 1

    if len(files) == 0:
        print(f"usage: {name} <output.srcjar> (<in.java>|<in.srcjar>)...")
        return 1

    with ZipFile(outfile, "w", compression=ZIP_STORED) as zip:
        for filename, content in sorted(files.items()):
            zip_info = ZipInfo(filename=filename, date_time=ZIP_EPOC)
            zip.writestr(zip_info, content)


def file_source(infiles):
    for infile in infiles:
        if infile.endswith(".java"):
            with open(infile, "rb") as f:
                yield infile, f.read()
        elif infile.endswith(".srcjar"):
            with ZipFile(infile, "r") as srcjar:
                for filename in srcjar.namelist():
                    yield filename, srcjar.read(filename)


def maybe_save(files, paths, filename, content):
    digest = hashlib.sha256(content).hexdigest()

    if filename not in paths:
        paths[filename] = digest
        files[filename] = content
    elif digest != paths[filename]:
        return DUPE


if __name__ == "__main__":
    import sys

    sys.exit(main(sys.argv) or 0)
