load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "market",
    srcs = glob([
        "*.java",
    ]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/enums",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/compliance/account_classification",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/compliance/account_classification/psd2_payment_account/result",
    ],
)
