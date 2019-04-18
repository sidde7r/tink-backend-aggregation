load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "ing",
    srcs = glob(["**/*.java"]),
    data = [
        "//data:aggregation_test",
        "//etc:development",
    ],
    tags = [
        "external",
        "manual",
    ],
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/be/banks/ing",
        "//src/integration/api",
        "//src/integration/lib",
        "//src/integration/lib:lib_test",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/storage",
        "//src/libraries/account_identifier",
        "//src/libraries/amount",
        "//src/libraries/date",
        "//src/libraries/i18n",
        "//src/libraries/serialization",
        "//src/libraries/transfer:enums",
        "//src/libraries/transfer:rpc",
        "@maven//:com_fasterxml_jackson_core_jackson_databind",
        "@maven//:com_google_guava_guava",
        "@maven//:org_assertj_assertj_core",
        "@maven//:org_mockito_mockito_core",
    ],
)
