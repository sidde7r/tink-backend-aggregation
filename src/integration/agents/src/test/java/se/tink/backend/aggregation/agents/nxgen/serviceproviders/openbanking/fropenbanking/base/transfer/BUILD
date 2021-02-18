load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "transfer",
    srcs = glob(["**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/fropenbanking/base",
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/lib",
        "//src/integration/models",
        "//src/libraries/account_identifier",
        "//src/libraries/serialization",
        "//third_party:lombok_libs",
        "//third_party:org_assertj_assertj_core",
    ],
)
