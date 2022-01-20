load("@tink_backend//tools/bzl:junit_strict.bzl", "junit_test")

junit_test(
    name = "execution_flow",
    srcs = glob(["*.java"]),
    data = [],
    resources = [],
    deps = [
        "//src/agent_sdk/sdk/src/steppable_execution",
    ],
)
