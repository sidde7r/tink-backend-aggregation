#!/bin/bash
set -euo pipefail

export SONAR_SCANNER_VERSION=4.2.0.1873
export SONAR_SCANNER_HOME=$HOME/.sonar/sonar-scanner-$SONAR_SCANNER_VERSION-linux
rm -rf $SONAR_SCANNER_HOME
mkdir -p $SONAR_SCANNER_HOME

curl -sSLo $HOME/.sonar/sonar-scanner.zip https://binaries.sonarsource.com/Distribution/sonar-scanner-cli/sonar-scanner-cli-$SONAR_SCANNER_VERSION-linux.zip

SONAR_SCANNER_SHA256SUM="44a5d985fc3bc10a8d4217160d2117289b7fe582acd410652b4bf59924593ce6"
echo "${SONAR_SCANNER_SHA256SUM} $HOME/.sonar/sonar-scanner.zip" | sha256sum -c

unzip $HOME/.sonar/sonar-scanner.zip -d $HOME/.sonar/
rm $HOME/.sonar/sonar-scanner.zip
export PATH=$SONAR_SCANNER_HOME/bin:$PATH
export SONAR_SCANNER_OPTS="-server"

BAZEL_OPTS="--workspace_status_command $(pwd)/stamp.sh
      --disk_cache=/cache/v4-disk
      --repository_cache=/cache/v4-repo
      --deleted_packages=deb,docker
      --curses=yes
      --color=yes"

if [ "$BUILDKITE_PULL_REQUEST" != "false" ]; then
  ./bazel-wrapper build \
    "$BAZEL_OPTS" \
     //src/...

  sonar-scanner \
    -Dsonar.pullrequest.key="$BUILDKITE_PULL_REQUEST" \
    -Dsonar.pullrequest.branch="$BUILDKITE_BRANCH" \
    -Dsonar.login="$SONAR_TOKEN"
else
  ./bazel-wrapper coverage \
      "$BAZEL_OPTS" \
      --collect_code_coverage \
      --combined_report=lcov \
      --coverage_report_generator=@bazel_sonarqube//:sonarqube_coverage_generator \
       //src/...

  sonar-scanner \
    -Dsonar.login="$SONAR_TOKEN" \
    -Dsonar.branch.name="$BUILDKITE_BRANCH" \
    -Dsonar.coverage.exclusions="" \
    -Dsonar.coverageReportPaths=bazel-out/_coverage/_coverage_report.dat
fi
