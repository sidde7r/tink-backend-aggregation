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
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/banks/n26",
        "//src/integration/amount",
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/credentials:service",
        "//src/integration/lib",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework",
        "//src/integration/models",
        "//src/libraries/agent-storage",
        "//src/libraries/serialization",
        "//third_party:org_mockito_mockito_core",
        "//third_party:org_slf4j_slf4j_api",
    ],
)
