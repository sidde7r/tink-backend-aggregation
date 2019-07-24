#!/bin/bash
set -e

handle_result()
{
    if [ $? -ne 0 ]; then
        echo "-->"
        echo "--> Java file(s) listed above are incorrectly formatted. Execute the following bazel command:"
        echo "--> bazel run :format"
        echo "--> You can also install the Intellij plugin google-java-format, configuring code style to AOSP:"
        echo "--> IntelliJ IDEA -> Preferences -> Other Settings -> google-java-format"
        echo "--> It is also recommended to enable 'Save actions' so that formatting is automatically done on save."
        echo "-->"
        exit 1
    fi
}

trap handle_result EXIT

./bazel-wrapper build //tools/format:google-java-format

find $(git rev-parse --show-toplevel) -type f -name "*.java" -not -path "*/tools/create_agent/*" \
    | xargs -n500 bazel-bin/tools/format/google-java-format --aosp --dry-run --set-exit-if-changed
