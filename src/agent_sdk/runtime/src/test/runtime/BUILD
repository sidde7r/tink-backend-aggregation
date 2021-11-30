load("@tink_backend//tools/bzl:junit_strict.bzl", "junit_test")

junit_test(
    name = "runtime",
    srcs = glob(["*.java"]),
    data = [],
    resources = [],
    deps = [
        "//src/agent_sdk/runtime",
        "//src/agent_sdk/sdk",
        "@agent_sdk_maven//:com_google_guava_guava",
        "@agent_sdk_maven//:com_google_inject_guice",
    ],
)
