load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "integration",
    srcs = glob(["*.java"]),
    data = [
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/pt/openbanking/caixa/integration/resources",
    ],
    visibility = ["//visibility:public"],
    runtime_deps = ["//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/pt/openbanking/caixa"],
    deps = [
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/pt/openbanking/module",
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/utils/fixtures",
        "//src/integration/enums",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework/compositeagenttest/wiremockpayment",
        "//src/integration/payment:rpc",
        "//third_party:com_github_tomakehurst_wiremock",
        "//third_party:lombok_libs",
        "//third_party:org_assertj_assertj_core",
        "//third_party:org_mockito_mockito_core",
        "//third_party:pl_pragmatists_junitparams",
        "@aggregation//:com_google_guava_guava",
        "@tink_backend//src/libraries/account_identifier",
    ],
)
