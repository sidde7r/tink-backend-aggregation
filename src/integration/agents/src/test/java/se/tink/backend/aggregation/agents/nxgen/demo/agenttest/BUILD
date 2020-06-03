load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "agenttest",
    srcs = glob(["**/*.java"]),
    data = [
        "//data:aggregation_test",
        "//data:provider_configurations",
        "//etc:development",
    ],
    tags = [
        "external",
        "manual",
    ],
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/credentials:service",
        "//src/integration/lib",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/utils/currency",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/nxgen/agents/agenttest",
    ],
)
