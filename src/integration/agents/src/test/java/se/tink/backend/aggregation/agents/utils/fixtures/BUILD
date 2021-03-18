load("//tools/bzl:junit.bzl", "junit_test")

java_library(
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
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/tools",
        "//src/integration/amount",
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/enums",
        "//src/integration/lib",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/configuration/agents",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/configuration/agentsservice",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework/compositeagenttest/base",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework/compositeagenttest/wiremockpayment",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework/compositeagenttest/wiremockrefresh",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/configuration",
        "//src/integration/payment:rpc",
        "//src/integration/transfer:enums",
        "//src/integration/transfer:rpc",
        "//third_party:lombok_libs",
        "//third_party:org_assertj_assertj_core",
        "//third_party:org_mockito_mockito_core",
        "@aggregation//:com_fasterxml_jackson_core_jackson_databind",
        "@aggregation//:com_google_guava_guava",
        "@tink_backend//src/libraries/account_identifier",
    ],
)
