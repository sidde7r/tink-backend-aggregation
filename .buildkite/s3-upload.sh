#!/bin/bash
set -e

S3_BUCKET="tink-repository"

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
    upload_package_s3 "tink-backend-aggregation" "debs/tink-backend-aggregation__amd64.deb"
    for pid in ${pids[*]}; do
      wait "$pid"
    done
    unset AWS_ACCESS_KEY_ID
    unset AWS_SECRET_ACCESS_KEY
    unset pids
fi
