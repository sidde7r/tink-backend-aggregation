#!/usr/bin/env bash

TEEFILE=$(mktemp)

# Make sure that all codeowners targets are referenced by the root generation rule
bazel query 'kind(codeowners, //...) except deps(//:generate_codeowners)' 2>&1 | tee "$TEEFILE" | grep "Empty results"

EXIT=$?

if [ "$EXIT" -eq 1 ]; then
    cat "$TEEFILE"
    echo "--> All codeowners rules must be depended on by //:generate_codeowners"
    echo "--> Add the codeowners as a dependency, or delete the target"
    exit 1
fi
