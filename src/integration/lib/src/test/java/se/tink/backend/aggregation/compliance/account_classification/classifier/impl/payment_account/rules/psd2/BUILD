load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "payment_account_psd2",
    srcs = glob(["*.java"]),
    deps = [
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/enums",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/compliance/account_capabilities",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/compliance/account_classification",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/compliance/account_classification/classifier/impl/payment_account/rules/psd2",
        "//third_party:junit_junit",
        "//third_party:org_assertj_assertj_core",
        "//third_party:org_mockito_mockito_core",
    ],
)
