load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "wrappers",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/aggregation/lib/src/main/java/se/tink/backend/aggregation/storage/database/models",
        "//third_party:com_google_guava_guava",
    ],
)
