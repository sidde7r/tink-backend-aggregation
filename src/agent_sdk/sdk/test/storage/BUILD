load("@tink_backend//tools/bzl:junit_strict.bzl", "junit_test")

junit_test(
    name = "storage",
    srcs = glob(["*.java"]),
    data = [],
    resources = [],
    deps = [
        "//src/agent_sdk/sdk/src/storage",
        "@agent_sdk_maven//:com_fasterxml_jackson_core_jackson_annotations",
        "@agent_sdk_maven//:com_fasterxml_jackson_core_jackson_core",
        "@agent_sdk_maven//:com_fasterxml_jackson_core_jackson_databind",
        "@tink_backend//src/libraries/serialization_utils",
    ],
)
