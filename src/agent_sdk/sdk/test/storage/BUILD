load("@tink_backend//tools/bzl:junit_strict.bzl", "junit_test")

junit_test(
    name = "storage",
    srcs = glob(["*.java"]),
    data = [],
    resources = [],
    deps = [
        "//src/agent_sdk/sdk/src/storage",
        "//src/libraries/serialization",
        "@aggregation//:com_fasterxml_jackson_core_jackson_annotations",
        "@aggregation//:com_fasterxml_jackson_core_jackson_core",
        "@aggregation//:com_fasterxml_jackson_core_jackson_databind",
    ],
)
