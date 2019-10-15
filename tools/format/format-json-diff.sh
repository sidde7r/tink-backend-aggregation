#!/usr/bin/env bash

# Do not run this directly. Instead, run the bazel rule //tools/format:format-json-diff or the alias
# :formatjson

formatter="$PWD/tools/format/jsonformat.py"

cd "$BUILD_WORKSPACE_DIRECTORY"

branch=$(git remote -v | grep -m 1 "tink-ab" | awk '{print $1"/master"}')

target_dir="data/seeding"

git diff --name-only --diff-filter=ACMR "${branch:-origin/master}" "$target_dir" \
    | grep "\.json$" \
    | xargs -n1000 python "$formatter"
