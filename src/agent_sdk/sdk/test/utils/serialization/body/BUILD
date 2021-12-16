load("@tink_backend//tools/bzl:junit_strict.bzl", "junit_test")

junit_test(
    name = "body",
    srcs = glob(["*.java"]),
    data = [],
    resources = [],
    deps = [
        "//src/agent_sdk/sdk/src/utils/serialization/body",
        "@aggregation//:com_fasterxml_jackson_core_jackson_annotations",
        "@aggregation//:com_sun_jersey_jersey_core",
    ],
)
