load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "danskebank",
    srcs = glob(["DanskebankNOAgentTest.java"]),
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
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/no/banks/danskebank",
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/credentials:service",
        "//src/integration/lib",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework",
        "//third_party:org_assertj_assertj_core",
        "//third_party:org_mockito_mockito_core",
    ],
)

junit_test(
    name = "danskebank-unit-tests",
    srcs = glob(
        ["**/*.java"],
        exclude = [
            "DanskebankNOAgentTest.java",
        ],
    ),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/no/banks/danskebank",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/banks/danskebank",
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/enums",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/compliance/account_capabilities",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/core/account",
        "//src/integration/models",
        "//src/libraries/serialization",
        "//third_party:org_assertj_assertj_core",
        "//third_party:org_mockito_mockito_core",
    ],
)
