load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "jollyday",
    srcs = glob(["**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "@aggregation//:de_jollyday_jollyday",
        "@aggregation//:joda_time_joda_time",
    ],
)
