load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "nordea",
    srcs = glob(["*.java"]),
    data = ["//:agent_test_data"],
    tags = [
        "external",
        "manual",
    ],
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/fi/openbanking/nordea",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/nordeabase",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework",
    ],
)

junit_test(
    name = "nordea-unittests",
    srcs = glob(
        ["**/*.java"],
        exclude = [
            "NordeaFiAgentTest.java",
        ],
    ),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/fi/openbanking/nordea",
        "//third_party:org_assertj_assertj_core",
        "//third_party:org_mockito_mockito_core",
        "@aggregation//:com_fasterxml_jackson_core_jackson_databind",
    ],
)
