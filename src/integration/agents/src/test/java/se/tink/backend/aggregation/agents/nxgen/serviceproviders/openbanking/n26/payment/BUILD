load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "n26_agent_tests",
    srcs = glob(["**/*.java"]),
    data = ["//:agent_test_data"],
    tags = [
        "external",
        "manual",
    ],
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/fr/openbanking/boursorama",
        "//src/integration/amount",
        "//src/integration/credentials:service",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework",
        "//src/integration/payment:rpc",
        "//src/integration/transfer:rpc",
        "//src/libraries/account_identifier",
    ],
)
