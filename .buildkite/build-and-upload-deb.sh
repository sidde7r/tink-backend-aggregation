#!/bin/bash

set -euo pipefail

TARGET=$1        # The bazel build label - //deb:main
DEB_PATH=$2 # The path to the built deb (not including bazel-bin or __amd64.deb) - deb/tink-backend-main
SERVICE_NAME=$3  # The full name of the service: tink-backend-main

FULL_DEB_PATH="bazel-bin/${DEB_PATH}__amd64.deb"

BUILDER_GPG_KEY_ID=${BUILDER_GPG_KEY_ID:-"ECF97785CF7BC5B6"}
BUILDER_GPG_S3_KEY_URL=${BUILDER_GPG_S3_KEY_URL:-"s3://tink-buildkite-secrets/builder/builder.gpg"}
S3_BUCKET="tink-repository"

echo "--- Generate versions file"
.buildkite/generate-version-file.sh

echo "--- Build deb package"
./bazel-wrapper build --disk_cache=/cache $TARGET

echo "--- Import GPG key"
# Import secret key if it's not in the GPG keychain
if ! gpg --list-secret-keys "$BUILDER_GPG_KEY_ID" &> /dev/null ; then
    aws configure set s3.signature_version s3v4
    aws s3 cp "$BUILDER_GPG_S3_KEY_URL" - | gpg --import
    # Verify that the imported key file did provide the key id
    gpg --list-secret-keys "$BUILDER_GPG_KEY_ID"
fi


echo "--- Sign package"
dpkg-sig -k "$BUILDER_GPG_KEY_ID" --sign builder "${FULL_DEB_PATH}"

upload_package_s3() {
    package_name=$1
    filename=$2
    cp "${filename}" "${package_name}_${VERSION}.deb"
    shasum -a 256 "${package_name}_${VERSION}.deb" > "${package_name}_${VERSION}.deb.sha256"
    aws s3 cp --acl bucket-owner-full-control "${package_name}_${VERSION}.deb.sha256" "s3://${S3_BUCKET}/backend/${VERSION}/${package_name}_${VERSION}.deb.sha256"
    aws s3 cp --acl bucket-owner-full-control "${package_name}_${VERSION}.deb" "s3://${S3_BUCKET}/backend/${VERSION}/${package_name}_${VERSION}.deb" &
    pids+=($!)
}

if aws s3 ls "s3://${S3_BUCKET}"; then
    echo "--- Upload packages S3"
    pids=()
    export AWS_ACCESS_KEY_ID="$TINK_REPO_UPLOAD_ACCESS_KEY_ID"
    export AWS_SECRET_ACCESS_KEY="$TINK_REPO_UPLOAD_SECRET_ACCESS_KEY"

    upload_package_s3 "${SERVICE_NAME}" "${FULL_DEB_PATH}"

    for pid in ${pids[*]}; do
      wait "$pid"
    done

    unset AWS_ACCESS_KEY_ID
    unset AWS_SECRET_ACCESS_KEY
    unset pids
fi
