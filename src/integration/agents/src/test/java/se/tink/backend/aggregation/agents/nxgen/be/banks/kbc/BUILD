load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "kbc",
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
        "//src/integration/account:enums",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/be/banks/kbc",
        "//src/integration/amount",
        "//src/integration/api",
        "//src/integration/lib",
        "//src/integration/lib:lib_test",
        "//src/integration/transfer:enums",
        "//src/integration/transfer:rpc",
        "//src/libraries/account_identifier",
        "//src/libraries/serialization",
        "//third_party:com_fasterxml_jackson_core_jackson_databind",
        "//third_party:org_assertj_assertj_core",
    ],
)
