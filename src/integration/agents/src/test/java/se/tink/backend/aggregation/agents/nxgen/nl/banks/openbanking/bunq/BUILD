load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "bunq",
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
    runtime_deps = [
        "@maven//:net_bytebuddy_byte_buddy",
        "@maven//:org_objenesis_objenesis",
    ],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/nl/banks/openbanking/bunq",
        "//src/integration/lib",
        "//src/integration/lib:lib_test",
        "//src/libraries/account_identifier",
        "//src/libraries/amount",
        "//src/libraries/payment:rpc",
        "@maven//:org_iban4j_iban4j",
        "@maven//:org_mockito_mockito_core",
    ],
)
