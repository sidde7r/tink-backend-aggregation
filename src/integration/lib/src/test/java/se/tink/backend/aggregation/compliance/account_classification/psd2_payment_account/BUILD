load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "psd2_payment_account",
    srcs = glob(["*.java"]),
    deps = [
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/compliance/account_classification",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/compliance/account_classification/psd2_payment_account",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/compliance/account_classification/psd2_payment_account/result",
        "//third_party:junit_junit",
        "//third_party:org_apache_commons_commons_lang3",
        "//third_party:org_assertj_assertj_core",
        "//third_party:org_mockito_mockito_core",
    ],
)
