#!/bin/bash

set -euo pipefail

export PATH="/usr/local/bin/google-cloud-sdk/bin:$PATH";
echo "$GOOGLE_CLOUD_ACCOUNT_JSON" | base64 --decode | gcloud auth activate-service-account --key-file=-;

./bazel-wrapper build docker:bundle.tar;
docker load -i bazel-bin/docker/bundle.tar;

docker tag gcr.io/tink-containers/tink-backend-aggregation:latest "gcr.io/tink-containers/tink-backend-aggregation:$VERSION";
gcloud docker -- push "gcr.io/tink-containers/tink-backend-aggregation:$VERSION";

docker tag gcr.io/tink-containers/tink-backend-provider-configuration:latest "gcr.io/tink-containers/tink-backend-provider-configuration:$VERSION";
gcloud docker -- push "gcr.io/tink-containers/tink-backend-provider-configuration:$VERSION";

docker build -t gcr.io/tink-containers/tink-backend-aggregation-statuspage-providers-cronjob jobs/cron/provider-status
docker tag gcr.io/tink-containers/tink-backend-aggregation-statuspage-providers-cronjob:latest "gcr.io/tink-containers/tink-backend-aggregation-statuspage-providers-cronjob:$VERSION";
gcloud docker -- push "gcr.io/tink-containers/tink-backend-aggregation-statuspage-providers-cronjob:$VERSION";
