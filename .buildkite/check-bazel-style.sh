#!/bin/bash

set -e

git clone https://github.com/bazelbuild/buildtools.git /go/src/github.com/bazelbuild/buildtools
git -C /go/src/github.com/bazelbuild/buildtools checkout 9f8fdb20dd423621ef00ced33dcb40204703c2c8
go install github.com/bazelbuild/buildtools/buildifier

if buildifier --mode=check $(find . -type f \( -iname BUILD -or -iname BUILD.bazel \)) ; then
    exit 0
else
    echo "-->"
    echo "--> At least one BUILD file is not correctly formatted, execute the following buildifier command:"
    echo "--> buildifier \$(find . -type f \( -iname BUILD -or -iname BUILD.bazel \))"
    echo "--> If you don't have buildifier installed, install it with: go get github.com/bazelbuild/buildtools/buildifier"
    echo "-->"
    exit 1
fi
