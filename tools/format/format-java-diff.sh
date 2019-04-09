#!/usr/bin/env bash
# Do not run this directly, instead run the bazel rule //tools/format:format-java-diff or the alias :format
formatter=$PWD/tools/format/google-java-format
cd $BUILD_WORKSPACE_DIRECTORY
branch=$(git remote -v | grep -m 1 "tink-ab" | awk '{print $1"/master"}')
git diff --name-only --diff-filter=ACMR ${branch:-origin/master} \
    | grep ".*\.java$" \
    | xargs -n1000 $formatter --aosp --replace
