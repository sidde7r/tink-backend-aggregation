load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "sbab-agent",
    srcs = glob(["SbabAgentTest.java"]),
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
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/se/banks/sbab",
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/credentials:service",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/configuration",
    ],
)

junit_test(
    name = "sbab",
    srcs = glob(
        ["**/*.java"],
        exclude = ["SbabAgentTest.java"],
    ),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/se/banks/sbab",
        "//src/integration/amount",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen",
        "//src/libraries/serialization",
        "//third_party:org_assertj_assertj_core",
    ],
)
