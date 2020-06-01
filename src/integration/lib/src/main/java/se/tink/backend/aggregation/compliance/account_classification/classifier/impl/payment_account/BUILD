java_library(
    name = "payment_account",
    srcs = glob(["*.java"]),
    visibility = [
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/compliance/account_classification:__subpackages__",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/compliance/account_classification:__subpackages__",
    ],
    deps = [
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/compliance/account_classification",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/compliance/account_classification/classifier/impl",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/compliance/account_classification/classifier/impl/payment_account/metrics",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/compliance/account_classification/classifier/impl/payment_account/rules/global",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/compliance/account_classification/classifier/impl/payment_account/rules/regulatory/psd2",
        "//src/libraries/metrics:registry",
    ],
)
