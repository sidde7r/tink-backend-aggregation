load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "test",
    srcs = glob(
        ["*.java"],
    ),
    data = [
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework/wiremock/utils/test/resources",
    ],
    visibility = ["//visibility:public"],
    runtime_deps = [
    ],
    deps = [
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework/wiremock",
    ],
)
