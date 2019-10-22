load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "sbab",
    srcs = glob(["**/*.java"]),
    tags = [
        "external",
        "manual",
    ],
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/se/banks/sbab",
        "//src/integration/api",
        "//src/integration/credentials:service",
        "//src/integration/lib:lib_test",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/configuration",
    ],
)
