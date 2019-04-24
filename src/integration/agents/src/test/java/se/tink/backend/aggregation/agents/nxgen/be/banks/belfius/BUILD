load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "belfius",
    srcs = glob(["**/*.java"]),
    data = [
        "//data:provider_configuration_test",
        "//etc:development",
    ],
    tags = [
        "external",
        "manual",
    ],
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/be/banks/belfius",
        "//src/integration/api",
        "//src/integration/lib",
        "//src/integration/lib:lib_test",
        "//src/libraries/account_identifier",
        "//src/libraries/amount",
        "//src/libraries/date",
        "//src/libraries/i18n",
        "//src/libraries/serialization",
        "//src/libraries/transfer:enums",
        "//src/libraries/transfer:rpc",
        "//src/libraries/user:rpc",
        "//third_party:org_mockito_mockito_core",
        "@com_fasterxml_jackson_core_jackson_databind//jar",
        "@com_google_guava_guava//jar",
        "@org_apache_commons_commons_lang3//jar",
    ],
)
