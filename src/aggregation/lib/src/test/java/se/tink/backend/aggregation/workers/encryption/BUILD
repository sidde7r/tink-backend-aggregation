load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "encryption",
    srcs = glob(["*.java"]),
    deps = [
        "//src/aggregation/aggregationcontroller_api/src/main/java/se/tink/backend/aggregation/aggregationcontroller",
        "//src/aggregation/lib/src/main/java/se/tink/backend/aggregation/storage/database/models",
        "//src/aggregation/lib/src/main/java/se/tink/backend/aggregation/workers/encryption",
        "//src/aggregation/lib/src/main/java/se/tink/backend/aggregation/wrappers",
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/credentials/src/main/java/se/tink/libraries/credentials/service",
        "//src/integration/user:rpc",
        "//src/libraries/cache",
        "//src/libraries/metrics:registry",
        "//src/libraries/serialization",
        "//third_party:com_google_guava_guava",
        "//third_party:org_mockito_mockito_core",
        "@tink_backend//src/libraries/encrypted-payload",
    ],
)
