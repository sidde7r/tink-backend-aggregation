load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "form",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//third_party:com_google_code_findbugs_jsr305",
        "@aggregation//:com_google_guava_guava",
    ],
)
