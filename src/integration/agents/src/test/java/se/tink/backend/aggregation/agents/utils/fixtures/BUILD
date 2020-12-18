load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "fixtures",
    srcs = glob(["*.java"]),
    data = [
        "//data:aggregation_test",
        "//data:provider_configurations",
    ],
    visibility = ["//visibility:public"],
    runtime_deps = [
        "//src/integration/agents",
    ],
    deps = [
        "//src/aggregation/api",
        "//src/integration/payment:rpc",
        "//src/integration/transfer:enums",
        "//src/integration/transfer:rpc",
        "//src/integration/amount",
        "//src/integration/enums",
        "//src/integration/lib",
        "//src/libraries/account_identifier",
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/tools",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/configuration/agents",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/configuration/agentsservice",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework/compositeagenttest/wiremockrefresh",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework/compositeagenttest/base",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework/compositeagenttest/wiremockpayment",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/configuration",
        "//third_party:org_assertj_assertj_core",
        "//third_party:org_mockito_mockito_core",
        "@aggregation//:com_fasterxml_jackson_core_jackson_databind",
        "@aggregation//:com_google_guava_guava",
        "//third_party:lombok_libs"],
)
