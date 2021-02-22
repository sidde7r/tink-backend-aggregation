load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "lunar",
    srcs = glob([
        "LunarDkAgentTest.java",
    ]),
    data = ["//:agent_test_data"],
    tags = [
        "external",
        "manual",
    ],
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/dk/banks/lunar",
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework",
    ],
)

junit_test(
    name = "lunar_unit_tests",
    srcs = glob(
        ["**/*.java"],
        exclude = [
            "LunarDkAgentTest.java",
        ],
    ),
    data = [
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/dk/banks/lunar/resources",
    ],
    visibility = ["//visibility:public"],
    deps = [
        "//src/aggregation/lib/src/main/java/se/tink/backend/aggregation/agents_platform/agents_framework",
        "//src/aggregation/lib/src/main/java/se/tink/backend/aggregation/agents_platform/framework",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/dk/banks/lunar",
        "//src/integration/amount",
        "//src/integration/lib",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/agentplatform",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/exceptions/agent",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/exceptions/bankservice",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/utils/encoding",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/controllers/authentication/multifactor/nemid",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/controllers/authentication/multifactor/thirdpartyapp/nemid/exception",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/response",
        "//src/integration/models",
        "//src/libraries/account_identifier",
        "//src/libraries/date",
        "//src/libraries/serialization",
        "//third_party:com_sun_jersey_jersey_core",
        "//third_party:commons_io_commons_io",
        "//third_party:lombok_libs",
        "//third_party:org_apache_commons_commons_collections4",
        "//third_party:org_apache_commons_commons_lang3",
        "//third_party:org_assertj_assertj_core",
        "//third_party:org_mockito_mockito_core",
        "//third_party:pl_pragmatists_junitparams",
        "@aggregation//:com_fasterxml_jackson_core_jackson_databind",
    ],
)
