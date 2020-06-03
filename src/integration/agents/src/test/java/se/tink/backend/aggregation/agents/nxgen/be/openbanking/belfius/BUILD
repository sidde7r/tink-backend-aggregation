load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "belfius",
    srcs = glob(["**/*.java"]),
    data = [
        "//data:aggregation_test",
        "//etc:development",
    ],
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/account/src/main/java/se/tink/libraries/account/enums",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/be/openbanking/belfius",
        "//src/integration/amount",
        "//src/integration/lib",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/configuration",
        "//src/integration/models",
        "//src/integration/payment:rpc",
        "//src/libraries/account_identifier",
        "//src/libraries/serialization",
        "//third_party:org_apache_commons_commons_lang3",
        "//third_party:org_mockito_mockito_core",
    ],
)
