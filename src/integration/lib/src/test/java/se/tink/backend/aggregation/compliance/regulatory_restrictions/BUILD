load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "regulatory_restrictions",
    srcs = glob(["*.java"]),
    deps = [
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/credentials/src/main/java/se/tink/libraries/credentials/service",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/compliance/account_classification/psd2_payment_account/result",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/compliance/regulatory_restrictions",
        "//src/libraries/metrics:core",
        "//src/libraries/metrics:registry",
        "//third_party:org_assertj_assertj_core",
        "//third_party:org_mockito_mockito_core",
    ],
)
