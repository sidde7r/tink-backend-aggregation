#!/bin/bash

# Explicitly not setting `-e` here to be able to contain errors from bazel-wrapper.
set -x

./bazel-wrapper test --disk_cache=/cache/v4-disk --repository_cache=/cache/v4-repo --deleted_packages=deb,docker --curses=yes --color=yes -- ...:all
OUTCOME=$?
.buildkite/upload-test-files.sh
exit $OUTCOME
