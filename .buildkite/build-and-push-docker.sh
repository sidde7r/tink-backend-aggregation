#!/bin/bash

set -euo pipefail

TARGET=$1

export PATH="/usr/local/bin/google-cloud-sdk/bin:$PATH"
echo "$GOOGLE_CLOUD_ACCOUNT_JSON" | base64 --decode | gcloud auth activate-service-account --key-file=-
yes | gcloud auth configure-docker || true

echo "--- Build and push image to gcr.io"
./bazel-wrapper run --workspace_status_command $(pwd)/stamp.sh --disk_cache=/cache/v4-disk --repository_cache=/cache/v4-repo $TARGET
