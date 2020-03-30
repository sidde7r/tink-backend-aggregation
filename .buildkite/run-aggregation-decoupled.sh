#!/bin/bash

# Explicitly not setting `-e` here to be able to contain errors from bazel-wrapper.
set -x

commit="$(git rev-parse HEAD)"

# Shared volume
LOGFILE="/cache/aggregation_decoupled-$commit.log"

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
    //src/aggregation/service:run_aggregation_decoupled_and_external_deps \
    &> "$LOGFILE"
