load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "controllers",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/aggregation/aggregationcontroller_api/src/main/java/se/tink/backend/aggregation/aggregationcontroller",
        "//src/aggregation/lib/src/main/java/se/tink/backend/aggregation/storage",
        "//src/aggregation/lib/src/main/java/se/tink/backend/aggregation/storage/database/models",
        "//src/aggregation/lib/src/main/java/se/tink/backend/aggregation/storage/database/providers",
        "//src/aggregation/lib/src/main/java/se/tink/backend/aggregation/storage/database/repositories",
        "//third_party:com_google_guava_guava",
        "//third_party:com_google_inject_guice",
        "//third_party:org_slf4j_slf4j_api",
    ],
)
