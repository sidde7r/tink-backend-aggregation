load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "handelsbanken",
    srcs = glob(["**/*.java"]),
    data = [
        "//data:aggregation_test",
        "//etc:development",
    ],
    tags = [
        "external",
        "manual",
    ],
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/se/business/handelsbanken",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/banks/handelsbanken",
        "//src/integration/api",
        "//src/integration/credentials:service",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/configuration",
    ],
)
