#!/bin/bash

# Explicitly not setting `-e` here to be able to contain errors from bazel-wrapper.
set -x

# Start aggregation service in the background
bazel run --workspace_status_command $(pwd)/stamp.sh --disk_cache=/cache/v4-disk --repository_cache=/cache/v4-repo --deleted_packages=deb,docker --curses=yes --color=yes -- //src/aggregation/service:bin_decoupled &

bazel_pid=$!

exit_code=1

while [ "$exit_code" != 0 ]; do
    # Ping continuously until service responds
    response="$(curl --silent localhost:9095/aggregation/ping)"
    exit_code="$?"
    sleep 1
done

# Stop aggregation service
kill $bazel_pid

# Don't proceed execution until aggregation process has stopped
wait

# Assert /ping responded with "pong"
if [ "$response" != "pong" ]; then
    exit 1
fi
