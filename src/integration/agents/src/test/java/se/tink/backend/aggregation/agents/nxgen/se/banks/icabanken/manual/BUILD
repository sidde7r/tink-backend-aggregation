load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "manual",
    srcs = glob(["*.java"]),
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
        "//src/integration/agents",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/se/banks/icabanken",
        "//src/integration/amount",
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/credentials:service",
        "//src/integration/lib",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework",
        "//src/integration/transfer:enums",
        "//src/integration/transfer:rpc",
        "//src/libraries/serialization",
        "//third_party:org_assertj_assertj_core",
        "@aggregation//:com_fasterxml_jackson_core_jackson_databind",
        "@aggregation//:commons_lang_commons_lang",
        "@tink_backend//src/libraries/account_identifier",
    ],
)
