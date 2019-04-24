#!/bin/bash

set -euo pipefail

export PATH="/usr/local/bin/google-cloud-sdk/bin:$PATH";
echo "$GOOGLE_CLOUD_ACCOUNT_JSON" | base64 --decode | gcloud auth activate-service-account --key-file=-;

echo "--- Build and push Statuspage Providers Cronjob image to gcr.io"
gcloud docker -- build -t "gcr.io/tink-containers/tink-backend-aggregation-statuspage-providers-cronjob:$VERSION" jobs/cron/provider-status;
gcloud docker -- push "gcr.io/tink-containers/tink-backend-aggregation-statuspage-providers-cronjob:$VERSION";

echo "--- Build and push Demo Bank Cronjob image to gcr.io"
gcloud docker -- build -t "gcr.io/tink-containers/tink-backend-aggregation-demo-bank-cronjob:$VERSION" jobs/cron/demo-bank;
gcloud docker -- push "gcr.io/tink-containers/tink-backend-aggregation-demo-bank-cronjob:$VERSION";

echo "--- Build and push Connectivity Cronjob image to gcr.io"
gcloud docker -- build -t "gcr.io/tink-containers/tink-backend-aggregation-connectivity-cronjob:$VERSION" jobs/cron/connectivity;
gcloud docker -- push "gcr.io/tink-containers/tink-backend-aggregation-connectivity-cronjob:$VERSION";
