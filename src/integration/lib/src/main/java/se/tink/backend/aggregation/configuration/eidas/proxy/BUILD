load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "proxy",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/annotations",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/configuration/eidas",
    ],
)
