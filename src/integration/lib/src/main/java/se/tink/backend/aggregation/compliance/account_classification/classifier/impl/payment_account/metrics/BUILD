java_library(
    name = "metrics",
    srcs = glob(["*.java"]),
    visibility = ["//src/integration/lib/src/main/java/se/tink/backend/aggregation/compliance/account_classification:__subpackages__"],
    deps = [
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/compliance/account_classification",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/compliance/account_classification/classifier/impl",
        "//src/libraries/metrics:core",
        "//src/libraries/metrics:registry",
    ],
)
