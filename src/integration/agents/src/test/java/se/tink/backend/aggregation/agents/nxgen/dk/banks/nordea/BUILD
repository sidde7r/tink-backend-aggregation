load("//tools/bzl:junit_old.bzl", "junit_test")

junit_test(
    name = "nordea",
    srcs = glob([
        "openbanking/NordeaDKAgentTest.java",
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
            "openbanking/NordeaDKAgentTest.java",
            "NordeaDkAgentTest.java",
        ],
    ),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/dk/banks/nordea",
        "//src/integration/lib",
        "//src/integration/models",
        "//third_party:org_assertj_assertj_core",
        "//third_party:org_mockito_mockito_core",
        "@aggregation//:com_fasterxml_jackson_core_jackson_databind",
    ],
)
