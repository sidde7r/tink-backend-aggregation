load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "belfius-unittests",
    srcs = glob(
        ["**/*.java"],
        exclude = [
            "BelfiusAgentTest.java",
            "integration/BelfiusDualAgentIntegrationTest.java",
        ],
    ),
    visibility = ["//visibility:public"],
    deps = [
        "//src/aggregation/lib/src/main/java/se/tink/backend/aggregation/agents_platform/agents_framework",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/be/banks/belfius",
        "//src/integration/amount",
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/exceptions",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/controllers/utils",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/core/account",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/storage",
        "//src/integration/user:rpc",
        "//src/libraries/serialization",
        "//third_party:lombok_libs",
        "//third_party:org_assertj_assertj_core",
        "//third_party:org_mockito_mockito_core",
    ],
)

junit_test(
    name = "belfius-agent",
    srcs = glob([
        "BelfiusAgentTest.java",
        "integration/BelfiusDualAgentIntegrationTest.java",
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
        "//src/aggregation/lib/src/main/java/se/tink/backend/aggregation/agents_platform/agents_framework",
        "//src/aggregation/lib/src/main/java/se/tink/backend/aggregation/agents_platform/framework",
        "//src/integration/agents",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/be/banks/belfius",
        "//src/integration/amount",
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/lib",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/storage",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/configuration",
        "//src/integration/transfer:enums",
        "//src/integration/transfer:rpc",
        "//src/integration/user:rpc",
        "//src/libraries/date",
        "//src/libraries/i18n",
        "//src/libraries/serialization",
        "//third_party:org_apache_commons_commons_lang3",
        "//third_party:org_assertj_assertj_core",
        "//third_party:org_mockito_mockito_core",
        "@aggregation//:com_fasterxml_jackson_core_jackson_databind",
        "@aggregation//:com_google_guava_guava",
        "@tink_backend//src/libraries/account_identifier",
    ],
)
