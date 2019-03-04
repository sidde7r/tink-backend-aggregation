#!/bin/bash
set -e

# The stamp script generates variables usable in Bazel BUILD files
#
# Read more: https://docs.bazel.build/versions/master/user-manual.html#flag--workspace_status_command

# The $VERSION variable is set by .buildkite/hooks/pre-command
# Default to $USER-DEVELOPMENT (eg: gustav-DEVELOPMENT) in non-Buildkite environment
echo "TINK_VERSION ${VERSION:-$USER-DEVELOPMENT}"
