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
    runtime_deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/no/openbanking/sdc",
    ],
    deps = [
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework",
    ],
)
