#!/bin/bash

# Explicitly not setting `-e` here to be able to contain errors from bazel-wrapper.
set -x

# Port exposed by application under test
PORT=9095

# Port exposed by application under test
CONTAINER_UNDER_TEST_NAME="appundertest"

teardown() {
    # Find a way to do this that doesn't couple this script with the name of the container
    appundertest_container_id="$(docker ps | grep $CONTAINER_UNDER_TEST_NAME | awk -F' ' '{print $1}')"

    # Tear down container under test
    docker stop "$appundertest_container_id"

    .buildkite/upload-test-files.sh

    # Upload aggregation service logs
    LOGFILE="/cache/aggregation_decoupled.log"
    buildkite-agent artifact upload "$LOGFILE"
}

# Building tests early so the test code can be built in parallel with the tested code
./bazel-wrapper build \
    --workspace_status_command $(pwd)/stamp.sh \
    --disk_cache=/cache/v4-disk \
    --repository_cache=/cache/v4-repo \
    --deleted_packages=deb,docker \
    --curses=yes \
    --color=yes \
    --test_output=streamed \
    --curses=no \
    -- \
    //src/aggregation/service/src/test/java/se/tink/backend/aggregation/service

build_outcome="$?"

if [ "$build_outcome" != 0 ]; then
    teardown
    exit 1
fi

echo "System tests starting..."

echo "Waiting for app under test to become healthy..."

exit_code=1

while [ "$exit_code" != 0 ]; do

    if [ "$exit_code" == 6 ]; then
        # Couldn't resolve host -> container stopped because service failed to start
        echo "Aggregation service seemed to have crashed upon boot."
        teardown
        exit 1
    fi

    # Ping continuously until service responds
    response="$(curl --silent $CONTAINER_UNDER_TEST_NAME:$PORT/aggregation/ping)"
    exit_code="$?"
    sleep 1
done

echo "App under test is now healthy. Testing..."

./bazel-wrapper test \
    --workspace_status_command $(pwd)/stamp.sh \
    --disk_cache=/cache/v4-disk \
    --repository_cache=/cache/v4-repo \
    --deleted_packages=deb,docker \
    --curses=yes \
    --color=yes \
    --test_output=streamed \
    --curses=no \
    -- \
    //src/aggregation/service/src/test/java/se/tink/backend/aggregation/service

test_outcome="$?"

echo "Testing finished. Stopping app under test..."

if [ "$test_outcome" != 0 ]; then
    teardown
    exit 1
fi

teardown

echo "System tests finished."
