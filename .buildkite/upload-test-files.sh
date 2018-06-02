#!/bin/sh
set -ex

buildkite-agent artifact upload "bazel-testlogs/**/test.log"

# JUnit XML output
buildkite-agent artifact upload "bazel-testlogs/**/test.xml"
