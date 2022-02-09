load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "transfer",
    srcs = glob(["**/*.java"]),
    tags = [
        "external",
        "manual",
    ],
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/se/banks/swedbank",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/exceptions/transfer",
        "//src/integration/transfer:rpc",
        "//src/libraries/i18n_aggregation",
        "//src/libraries/serialization",
        "//third_party:com_google_http_client_google_http_client",
        "//third_party:org_assertj_assertj_core",
        "//third_party:org_mockito_mockito_core",
    ],
)
