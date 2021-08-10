#!/bin/bash

set -euo pipefail

TARGET=$1
CONFIG=$2

export PATH="/usr/local/bin/google-cloud-sdk/bin:$PATH"
echo "$GOOGLE_CLOUD_ACCOUNT_JSON" | base64 --decode | gcloud auth activate-service-account --key-file=-
yes | gcloud auth configure-docker || true

echo "--- Generate versions file"
.buildkite/generate-version-file.sh

echo "--- Build and push image to gcr.io"
if [ "$CONFIG" = "jdk11" ]; then
  ./bazel-wrapper run --workspace_status_command $(pwd)/stamp.sh --disk_cache=/cache/v4-disk --config=jdk11 --repository_cache=/cache/v4-repo $TARGET
else
  ./bazel-wrapper run --workspace_status_command $(pwd)/stamp.sh --disk_cache=/cache/v4-disk --repository_cache=/cache/v4-repo $TARGET
fi
