#!/bin/bash
set -ex

cp /tink-backend/docker/backend-integration-tests/development.yml etc/development.yml
cp /tink-backend/docker/kirkby/development-connector-server.yml etc/kirkby/development-connector-server.yml
cp /tink-backend/docker/seb/development-connector-server.yml etc/seb/development-connector-server.yml
bazel run :system seed-database /tink-backend/etc/development.yml

set +e
./bazel-wrapper test --deleted_packages=deb,docker --curses=yes --color=yes -- :integration-test
OUTCOME=$?
.buildkite/upload-test-files.sh
exit $OUTCOME
