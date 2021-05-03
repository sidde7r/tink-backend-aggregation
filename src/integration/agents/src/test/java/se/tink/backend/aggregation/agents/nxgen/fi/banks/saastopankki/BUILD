load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "saastopankki",
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
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/fi/banks/saastopankki",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/banks/samlink",
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/credentials:service",
        "//src/integration/lib",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/contexts/agent",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/logmasker",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/client",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework/context",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/utils/currency",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/nxgen/agents/agenttest",
        "//src/libraries/agent-storage",
        "//third_party:com_github_tomakehurst_wiremock",
        "//third_party:commons_io_commons_io",
        "//third_party:org_mockito_mockito_core",
        "//third_party:org_slf4j_slf4j_api",
    ],
)
