java_library(
    name = "psd2",
    srcs = glob(["*.java"]),
    visibility = [
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/compliance/account_classification:__subpackages__",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/compliance/account_classification:__subpackages__",
    ],
    deps = [
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/enums",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/compliance/account_capabilities",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/compliance/account_classification",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/compliance/account_classification/classifier/impl",
        "//third_party:com_google_guava_guava",
    ],
)
