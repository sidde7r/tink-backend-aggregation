#!/bin/bash

# Explicitly not setting `-e` here to be able to contain errors from bazel-wrapper.
set -x

./bazel-wrapper test --deleted_packages=deb,docker --curses=yes --color=yes -- ...:all
OUTCOME=$?
.buildkite/upload-test-files.sh
exit $OUTCOME
