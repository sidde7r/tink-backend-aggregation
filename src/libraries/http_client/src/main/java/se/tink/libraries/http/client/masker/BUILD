load("//tools/bzl:junit.bzl", "junit_test")

java_library(
    name = "masker",
    srcs = glob(["**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/aggregation/lib/src/main/java/se/tink/backend/aggregation/agents_platform/framework",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/utils/crypto/hash",
        "//src/libraries/request_tracing",
        "//third_party:com_sun_jersey_jersey_client",
        "//third_party:com_sun_jersey_jersey_core",
        "//third_party:com_sun_jersey_jersey_server",
        "//third_party:eu_geekplace_javapinning_java_pinning_jar",
        "//third_party:org_slf4j_slf4j_api",
        "@aggregation//:com_google_code_gson_gson",
        "@aggregation//:com_google_guava_guava",
    ],
)
