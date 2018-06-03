#!/bin/bash
set -e

BUILDER_GPG_KEY_ID=${BUILDER_GPG_KEY_ID:-"ECF97785CF7BC5B6"}
BUILDER_GPG_S3_KEY_URL=${BUILDER_GPG_S3_KEY_URL:-"s3://tink-buildkite-secrets/builder/builder.gpg"}

sign_deb_package() {
    dpkg-sig -k "$BUILDER_GPG_KEY_ID" --sign builder "$1" &
    pids+=($!)
}

echo "--- Import GPG key"
# Import secret key if it's not in the GPG keychain
if ! gpg --list-secret-keys "$BUILDER_GPG_KEY_ID" &> /dev/null ; then
    aws configure set s3.signature_version s3v4
    aws s3 cp "$BUILDER_GPG_S3_KEY_URL" - | gpg --import
    # Verify that the imported key file did provide the key id
    gpg --list-secret-keys "$BUILDER_GPG_KEY_ID"
fi

echo "--- Sign packages"
pids=()
sign_deb_package "debs/tink-backend-aggregation__amd64.deb"
for pid in ${pids[*]}; do
  wait "$pid"
done
unset pids
