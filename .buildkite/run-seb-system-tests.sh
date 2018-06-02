#!/bin/bash
set -ex

cp /tink-backend/docker/seb/development-connector-server.yml etc/seb/development-connector-server.yml
cp /tink-backend/docker/seb/development-main-server.yml etc/seb/development-main-server.yml
cp /tink-backend/docker/seb/development-system-server.yml etc/seb/development-system-server.yml

bazel run :system seed-database /tink-backend/docker/backend-system-tests/seb-seed-database.yml

set +e
./bazel-wrapper test --test_filter=se.tink.backend.connector.SEBConnectorSystemTest --deleted_packages=deb,docker --curses=yes --color=yes -- :system-test
OUTCOME=$?
.buildkite/upload-test-files.sh
exit $OUTCOME
