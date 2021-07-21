load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "identification",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/libraries/metrics:core",
        "@aggregation//:com_google_guava_guava",
    ],
)
