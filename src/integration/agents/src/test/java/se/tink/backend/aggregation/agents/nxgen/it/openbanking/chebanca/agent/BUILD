load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "agent",
    srcs = glob(["**/*.java"]),
    data = ["//:agent_test_data"],
    tags = [
        "external",
        "manual",
    ],
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/it/openbanking/chebanca",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework",
    ],
)
