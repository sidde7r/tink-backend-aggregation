load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "demoaccountgeneratortest",
    srcs = glob(["**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/credentials:service",
        "//src/integration/lib",
        "//src/libraries/account_identifier",
        "//third_party:org_assertj_assertj_core",
        "//third_party:org_iban4j_iban4j",
        "@aggregation//:commons_validator_commons_validator",
    ],
)
