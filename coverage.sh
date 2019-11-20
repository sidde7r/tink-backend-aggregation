#!/bin/bash

set -e

# Usage: ./coverage.sh [ --upload | --local_web ] bazel-target
#
# --upload     Upload the coverage report to codecov
# --local_web  Generates a local HTML report with genhtml, and opens it in your
#              browser.
# --disk_cache Forwarded option to bazel
#
# bazel-target can only be set once, and must be a valid Bazel selector such as
# //src/... or //src/main/lib:lib-test

TEST_TARGET=$1 # bazel path to the test to run
LOCAL_WEB=0; # if we should run genhtml and open the result
CODECOV_UPLOAD=0; # if the the combined lcov file should be uploaded to codecov
DISK_CACHE_ARG=""; #

# Parse options
while [[ "$#" -gt 0 ]]; do case $1 in
    --upload) CODECOV_UPLOAD=1; shift;;
    --local_web) LOCAL_WEB=1; shift;;

    # Support both "--disk_cache=/foo" and "--disk_cache /foo"
    --disk_cache) DISK_CACHE_ARG="--disk_cache=$2"; shift; shift;;
    --disk_cache=*) DISK_CACHE_ARG="$1"; shift;;

    # Options that doesn't match anything else is the test target
    *) TEST_TARGET=$1; shift;;
  esac;
done

if ! [ -x "$(command -v lcov)" ]; then
    echo "'lcov' is not installed"
    exit 1
fi

testlogs_dir=$(bazel info bazel-testlogs)

echo "--- Running tests in coverage mode"

# bazel-wrapper forwards stderr to stdout, `tee` is used directly after bazel
# is run. This sends a copy of both bazels stdout and stderr to "our" stderr
# so that you can follow the progress "live" when running this command.

# shellcheck disable=SC2086
coverage_files=$(./bazel-wrapper coverage \
    $DISK_CACHE_ARG "${TEST_TARGET}" \
    | tee /dev/stderr \
    | grep "${testlogs_dir}" \
    | grep "/coverage.dat")

sum_coverage_file=$(mktemp)

# Merge all lcov files
echo "--- Merge lcov files"
# Add -a in front of every coverage.dat file, and execute lcov once
# shellcheck disable=SC2086
echo $coverage_files | xargs -n1 echo "-a" | xargs lcov -o "${sum_coverage_file}"

echo "Saved coverage summary to ${sum_coverage_file}"

if [ $CODECOV_UPLOAD -eq 1 ]; then
    echo "--- Uploading to CodeCov"
    bash .buildkite/codecov-upload.sh -f "${sum_coverage_file}"
fi

if [ $LOCAL_WEB -eq 1 ]; then
    echo "--- Building with genhtml"
    gen_out_dir=$(mktemp -d)
    genhtml -o "${gen_out_dir}" "${sum_coverage_file}"
    open "${gen_out_dir}/index.html"
fi

exit 0;
