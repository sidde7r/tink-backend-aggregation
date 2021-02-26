#!/bin/bash

# Need to access gcr in order to fetch the openjdk8 image which in turn is needed to build
# //src/aggregation/service:aggregation_decoupled_image.tar
export PATH="/usr/local/bin/google-cloud-sdk/bin:$PATH"
echo "$GOOGLE_CLOUD_ACCOUNT_JSON" | base64 --decode | gcloud auth activate-service-account --key-file=-
yes | gcloud auth configure-docker || true

# Explicitly not setting `-e` here to be able to contain errors from bazel-wrapper.
set -x

# Testcontainers needs to find this executable in the PATH that Bazel looks at (e.g. /bin/)
ln -s /usr/local/bin/google-cloud-sdk/bin/docker-credential-gcloud /bin/docker-credential-gcloud

./bazel-wrapper test \
    --workspace_status_command $(pwd)/stamp.sh \
    --disk_cache=/cache/v4-disk \
    --repository_cache=/cache/v4-repo \
    --deleted_packages=deb,docker \
    --curses=yes \
    --color=yes \
    -- \
    //src/aggregation/service/src/test/java/se/tink/backend/aggregation/service:ci

OUTCOME=$?
.buildkite/upload-test-files.sh
exit $OUTCOME
