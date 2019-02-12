#!/bin/bash

set -euo pipefail

.buildkite/generate-version-file.sh
echo "--- Build deb packages"
./bazel-wrapper build --disk_cache=/cache deb:all
mkdir -p debs/
cp bazel-bin/deb/tink-backend-* debs/
.buildkite/sign-debs.sh
.buildkite/s3-upload.sh
