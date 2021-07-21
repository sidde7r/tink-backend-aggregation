load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "jersey",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/aggregation/api",
        "//src/aggregation/api/src/main/java/se/tink/backend/aggregation/cluster/annotations",
        "//src/aggregation/api/src/main/java/se/tink/backend/aggregation/cluster/identification",
        "//src/aggregation/lib/src/main/java/se/tink/backend/aggregation/storage/database/models",
        "//src/aggregation/lib/src/main/java/se/tink/backend/aggregation/storage/database/providers",
        "//third_party:com_google_guava_guava",
        "//third_party:com_google_inject_guice",
        "//third_party:com_sun_jersey_jersey_core",
        "//third_party:com_sun_jersey_jersey_server",
        "//third_party:org_slf4j_slf4j_api",
    ],
)
