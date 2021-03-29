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

# contain errors from bazel
set +e
echo "--- Build tink-backend-aggregation"
time ./bazel-wrapper build \
    --build_manual_tests \
    --workspace_status_command $(pwd)/stamp.sh \
    --deleted_packages=deb,docker \
    --curses=yes \
    --color=yes

echo "--- Run coverage on tink-backend-aggregation"
time ./bazel-wrapper coverage \
  --curses=yes \
  --color=yes \
  --keep_going \
  --test_lang_filters=java \
  --test_size_filters=-large,-enormous \
  --collect_code_coverage \
  --combined_report=lcov \
  --coverage_report_generator=@bazel_sonarqube//:sonarqube_coverage_generator \
  //src/...

set -e

sonar-scanner \
  -Dsonar.login="$SONAR_TOKEN" \
  -Dsonar.branch.name="$BUILDKITE_BRANCH" \
  -Dsonar.coverage.exclusions="" \
  -Dsonar.coverageReportPaths=bazel-out/_coverage/_coverage_report.dat
