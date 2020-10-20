load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "dnb-agent",
    srcs = glob([
        "DnbAgentTest.java",
        "DnbDualAgentTest.java",
    ]),
    data = [
        "//data:aggregation_test",
        "//data:cryptography_test",
        "//data:provider_configurations",
        "//etc:development",
    ],
    tags = [
        "external",
        "manual",
    ],
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/no/banks/dnb",
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/credentials:service",
        "//src/integration/lib",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/storage",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/configuration",
        "//src/integration/user:rpc",
        "//third_party:com_github_tomakehurst_wiremock",
        "//third_party:org_mockito_mockito_core",
    ],
)

junit_test(
    name = "dnb-unit",
    srcs = glob(
        ["**/*.java"],
        exclude = [
            "DnbAgentTest.java",
            "DnbDualAgentTest.java",
        ],
    ),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/no/banks/dnb",
        "//third_party:org_assertj_assertj_core",
    ],
)
