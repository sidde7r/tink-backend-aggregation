load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "handler",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//third_party:org_apache_httpcomponents_httpcore",
    ],
)
