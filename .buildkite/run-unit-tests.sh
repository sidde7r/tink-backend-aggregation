#!/bin/bash

# Explicitly not setting `-e` here to be able to contain errors from bazel-wrapper.
set -x

# Ensure that tests tagged "manual" can be built
./bazel-wrapper build \
    --build_manual_tests \
    --workspace_status_command $(pwd)/stamp.sh \
    --remote_cache=http://bazels3cache:7777/ \
    --deleted_packages=deb,docker \
    --curses=yes \
    --color=yes \
    -- \
    //src/integration/agents/...

BUILD_OUTCOME=$?

if [ $BUILD_OUTCOME -ne 0 ]; then
    exit $BUILD_OUTCOME
fi

./bazel-wrapper test \
    --workspace_status_command $(pwd)/stamp.sh \
    --remote_cache=http://bazels3cache:7777/ \
    --deleted_packages=deb,docker \
    --curses=yes \
    --color=yes \
    -- \
    ...:all
OUTCOME=$?
.buildkite/upload-test-files.sh
exit $OUTCOME
