#!/bin/bash

set -euo pipefail

main() {
    local service_names=("$@")
    for service_name in "${service_names[@]}"; do
        /go/bin/kubernetes-generator --mode push --version "${VERSION}" --repo . --chart "${service_name}";
    done
}

main "$@"
