load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "locks",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "@aggregation//:com_fasterxml_jackson_core_jackson_annotations",
        "@aggregation//:com_google_guava_guava",
    ],
)
