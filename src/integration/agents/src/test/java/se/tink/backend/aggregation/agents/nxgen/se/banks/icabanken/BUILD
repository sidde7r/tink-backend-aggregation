load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "icabanken",
    srcs = glob(["**/*.java"]),
    data = [
        "//data:aggregation_test",
        "//data:cryptography_test",
        "//data:provider_configurations",
        "//etc:development",
    ],
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/se/banks/icabanken",
        "//src/integration/amount",
        "//src/integration/credentials:service",
        "//src/integration/lib",
        "//src/integration/transfer:enums",
        "//src/integration/transfer:rpc",
        "//src/libraries/account_identifier",
        "//third_party:org_assertj_assertj_core",
        "@aggregation//:com_fasterxml_jackson_core_jackson_databind",
        "@aggregation//:commons_lang_commons_lang",
    ],
)
