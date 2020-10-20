#!/bin/bash
set -euo pipefail

export SONAR_SCANNER_VERSION=4.5.0.2216
export SONAR_SCANNER_HOME=/cache/sonar-scanner-$SONAR_SCANNER_VERSION-linux

if [ ! -d "$SONAR_SCANNER_HOME" ]; then
  curl -sSLo /cache/sonar-scanner.zip https://binaries.sonarsource.com/Distribution/sonar-scanner-cli/sonar-scanner-cli-$SONAR_SCANNER_VERSION-linux.zip
  SONAR_SCANNER_SHA256SUM="ae6bc36f997604657c8fde1486eda4257c280c01c0dd59dc01a86a1af2d8dc8d"
  echo "${SONAR_SCANNER_SHA256SUM} /cache/sonar-scanner.zip" | sha256sum -c
  unzip /cache/sonar-scanner.zip -d /cache/
  rm /cache/sonar-scanner.zip
fi

export PATH=$SONAR_SCANNER_HOME/bin:$PATH
export SONAR_SCANNER_OPTS="-server"

./bazel-wrapper build \
    --workspace_status_command $(pwd)/stamp.sh \
    --disk_cache=/cache/v4-disk \
    --repository_cache=/cache/v4-repo \
    --deleted_packages=deb,docker \
    --curses=yes \
    --color=yes \
    -- \
    //src/...

if [ "$BUILDKITE_PULL_REQUEST" != "false" ]; then
  sonar-scanner \
    -Dsonar.pullrequest.key="$BUILDKITE_PULL_REQUEST" \
    -Dsonar.pullrequest.branch="$BUILDKITE_BRANCH" \
    -Dsonar.login="$SONAR_TOKEN"
else
  sonar-scanner \
    -Dsonar.login="$SONAR_TOKEN" \
    -Dsonar.branch.name="$BUILDKITE_BRANCH"
fi
