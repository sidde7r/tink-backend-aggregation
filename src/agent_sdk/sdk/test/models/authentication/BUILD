load("@tink_backend//tools/bzl:junit_strict.bzl", "junit_test")

junit_test(
    name = "authentication",
    srcs = glob(["*.java"]),
    data = [],
    resources = [],
    deps = [
        "//src/agent_sdk/libs:serialization_utils",
        "//src/agent_sdk/sdk/src/models/authentication",
    ],
)
