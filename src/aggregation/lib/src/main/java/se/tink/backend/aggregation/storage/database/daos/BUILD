load("@rules_java//java:defs.bzl", "java_library")
load("//tools/bzl:junit.bzl", "junit_test")

java_library(
    name = "daos",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/aggregation/lib/src/main/java/se/tink/backend/aggregation/storage/database/models",
        "//src/aggregation/lib/src/main/java/se/tink/backend/aggregation/storage/database/repositories",
        "//src/aggregation/lib/src/main/java/se/tink/backend/aggregation/wrappers",
        "//third_party:com_google_guava_guava",
        "//third_party:javax_inject_javax_inject",
        "//third_party:org_slf4j_slf4j_api",
    ],
)
