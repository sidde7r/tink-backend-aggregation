load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "at-ing-test",
    srcs = glob(["**/*.java"]),
    tags = [
        "external",
        "manual",
    ],
    visibility = ["//visibility:public"],
    runtime_deps = [
        "//:aggregation-lib",
    ],
    deps = [
        "//:aggregation-api",
        "//:aggregation-test",
        "//src/agents-lib/src/main/java/se/tink/backend/aggregation/agents/nxgen/at/banks/ing:at-ing-lib",
    ],
)
