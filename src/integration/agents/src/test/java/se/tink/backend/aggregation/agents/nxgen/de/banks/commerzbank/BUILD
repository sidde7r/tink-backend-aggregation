load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "commerzbank-agent",
    srcs = glob(["CommerzbankAgentTest.java"]),
    data = ["//:agent_test_data"],
    tags = [
        "external",
        "manual",
    ],
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework",
    ],
)

junit_test(
    name = "commerzbank",
    srcs = glob(
        ["**/*.java"],
        exclude = ["CommerzbankAgentTest.java"],
    ),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/de/banks/commerzbank",
        "//src/integration/amount",
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/exceptions",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/controllers/utils",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/storage",
        "//src/libraries/i18n",
        "//src/libraries/serialization",
        "//third_party:org_assertj_assertj_core",
        "//third_party:org_mockito_mockito_core",
        "@aggregation//:com_fasterxml_jackson_core_jackson_databind",
    ],
)
