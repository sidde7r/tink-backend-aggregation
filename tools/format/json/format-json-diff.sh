#!/usr/bin/env bash

# Do not run this directly. Instead, run the bazel rule //tools/format/json:format-json-diff or the alias
# :formatjson

formatter="$PWD/tools/format/json/jsonformat.py"

cd "$BUILD_WORKSPACE_DIRECTORY"

branch=$(git remote -v | grep -m 1 "tink-ab" | awk '{print $1"/master"}')

target_dir="data/seeding"

files_to_format=$(git diff --name-only --diff-filter=ACMR "${branch:-origin/master}" "$target_dir" | grep "\.json")

if [ ! -z "$files_to_format" ]; then
    python3 "$formatter" $files_to_format
fi
