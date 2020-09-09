#!/bin/bash

set -euo pipefail

main() {
    local service_names=("$@")
    for service_name in "${service_names[@]}"; do
        if [[ ! -f ".charts/${service_name}/Chart.yaml" ]]; then
            continue
        fi
        /go/bin/kubernetes-generator --mode push --version "${VERSION}" --repo . --chart "${service_name}";
    done
}

main "$@"
