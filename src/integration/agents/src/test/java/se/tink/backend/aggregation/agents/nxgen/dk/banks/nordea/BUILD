load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "nordea",
    srcs = glob([
        "NordeaDkAgentTest.java",
    ]),
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
    name = "nordea-unit-tests",
    srcs = glob(
        ["**/*.java"],
        exclude = [
            "NordeaDkAgentTest.java",
        ],
    ),
    data = [
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/dk/banks/nordea/resources",
    ],
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/dk/banks/nordea",
        "//src/integration/amount",
        "//src/integration/lib",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/response",
        "//src/integration/models",
        "//src/libraries/date",
        "//src/libraries/serialization",
        "//third_party:io_vavr",
        "//third_party:lombok_libs",
        "//third_party:org_assertj_assertj_core",
        "//third_party:org_mockito_mockito_core",
        "//third_party:org_slf4j_slf4j_api",
        "//third_party:pl_pragmatists_junitparams",
        "@aggregation//:com_fasterxml_jackson_core_jackson_databind",
        "@tink_backend//src/libraries/account_identifier",
    ],
)
