#!/usr/bin/env bash
set -xeuo pipefail

echo "--- Creating archive of git-mirror directory"
REPO=git-github-com-tink-ab-tink-backend-aggregation-git
CURDATE=$(date "+%Y%m%d-%H%M%S")
TARFILE="$REPO.$BUILDKITE_COMMIT.$CURDATE.tar"
tar -cvf "$TARFILE" \
  -C /var/lib/buildkite-agent/git-mirrors \
  "$REPO"

echo -n "$TARFILE" > "$REPO"

echo "--- Uploading to S3"
aws s3 cp "$TARFILE" \
  "s3://tink-build-shared-data-build-production/$TARFILE"

aws s3 cp "$REPO" "s3://tink-build-shared-data-build-production/$REPO"
rm "$TARFILE"
rm "$REPO"
