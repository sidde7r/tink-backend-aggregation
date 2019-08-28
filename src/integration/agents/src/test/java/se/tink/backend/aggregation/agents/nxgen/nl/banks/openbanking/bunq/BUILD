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
        "//third_party:net_bytebuddy_byte_buddy",
        "//third_party:org_objenesis_objenesis",
    ],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/nl/banks/openbanking/bunq",
        "//src/integration/lib",
        "//src/integration/lib:lib_test",
        "//src/integration/payment:rpc",
        "//src/libraries/account_identifier",
        "//src/libraries/amount",
        "//third_party:org_iban4j_iban4j",
        "//third_party:org_mockito_mockito_core",
    ],
)
