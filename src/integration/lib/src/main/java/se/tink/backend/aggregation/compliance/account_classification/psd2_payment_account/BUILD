load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "psd2_payment_account",
    srcs = glob(["*.java"]),
    visibility = [
        "//visibility:public",
    ],
    deps = [
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/compliance/account_classification",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/compliance/account_classification/psd2_payment_account/rules/common",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/compliance/account_classification/psd2_payment_account/rules/market",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/compliance/account_classification/psd2_payment_account/result",
        "//src/libraries/metrics:core",
        "//src/libraries/metrics:registry",
        "//third_party:com_google_guava_guava",

        # JSR305 (javax.annotations)
        "//third_party:com_google_code_findbugs_jsr305",
    ],
)
