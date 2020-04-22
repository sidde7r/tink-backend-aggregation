#!/bin/bash

# Explicitly not setting `-e` here to be able to contain errors from bazel-wrapper.
set -x

commit="$(git rev-parse HEAD)"

# Shared volume
LOGFILE="/cache/fake_aggregation_controller-$commit.log"

./bazel-wrapper run \
    --workspace_status_command $(pwd)/stamp.sh \
    --disk_cache=/cache/v4-disk \
    --repository_cache=/cache/v4-repo \
    --deleted_packages=deb,docker \
    --curses=yes \
    --color=yes \
    --test_output=streamed \
    --curses=no \
    -- \
    //src/aggregation/service/src/test/java/se/tink/backend/aggregation/aggregationcontroller:fake_aggregationcontroller_bin \
    2>&1 | tee "$LOGFILE"
#    &> "$LOGFILE"
