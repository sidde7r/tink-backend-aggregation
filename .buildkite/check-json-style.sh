#!/bin/bash
set -e

handle_result()
{
    if [ $? -ne 0 ]; then
        echo "-->"
        echo "--> JSON file(s) listed above are incorrectly formatted. Execute the following bazel command:"
        echo "--> bazel run :formatjson"
        echo "--> You can also configure IntelliJ to use 4-space indentation:"
        echo "--> IntelliJ IDEA -> Preferences -> Editor -> Code Style -> JSON"
        echo "--> It is also recommended to enable 'Save actions' so that formatting is automatically done on save."
        echo "-->"
        exit 1
    fi
}

trap handle_result EXIT

./bazel-wrapper run //tools/format:format-json-diff

exit $(git diff-index --quiet HEAD --)
