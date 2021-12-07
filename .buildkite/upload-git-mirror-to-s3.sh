#!/usr/bin/env bash
set -xeuo pipefail

echo "--- Creating archive of git-mirror directory"
tar -cvf git-github-com-tink-ab-tink-backend-aggregation-git.tar \
  -C /var/lib/buildkite-agent/git-mirrors \
  git-github-com-tink-ab-tink-backend-aggregation-git

echo "--- Uploading to S3"
aws s3 cp git-github-com-tink-ab-tink-backend-aggregation-git.tar \
  s3://tink-build-shared-data-build-production/git-github-com-tink-ab-tink-backend-aggregation-git.tar

rm git-github-com-tink-ab-tink-backend-aggregation-git.tar
