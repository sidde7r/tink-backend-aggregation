#!/bin/bash
set -euo pipefail

# The stamp script generates variables usable in Bazel BUILD files
#
# Read more: https://docs.bazel.build/versions/master/user-manual.html#flag--workspace_status_command

# The $VERSION variable is set by .buildkite/hooks/pre-command
# Default to $USER-DEVELOPMENT (eg: gustav-DEVELOPMENT) in non-Buildkite environment
echo "TINK_VERSION ${VERSION:-$USER-DEVELOPMENT}"

# Get minikube IP, the variable will be empty if minikube is not running
# Both MINIKUBE_NODE_IP and ENVSUBST_PATH are _stable_ which means that the cache
# will be invalidated if the value changes
MINIKUBE_IP=$(minikube ip 2> /dev/null || true)

echo "STABLE_AGGREGATIONDB_HOSTPORT ${OVERRIDE_AGGREGATIONDB_HOSTPORT:-${MINIKUBE_IP}:31001}"
echo "STABLE_COORDINATION_HOSTPORT ${OVERRIDE_COORDINATION_HOSTPORT:-${MINIKUBE_IP}:31003}"
echo "STABLE_CACHE_HOSTPORT ${OVERRIDE_CACHE_HOSTPORT:-${MINIKUBE_IP}:31005}"

echo "STABLE_ENVSUBST_PATH $(which envsubst)"

exit 0
