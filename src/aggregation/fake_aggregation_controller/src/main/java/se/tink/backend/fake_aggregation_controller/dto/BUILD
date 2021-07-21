load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "dto",
    srcs = glob(["**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//third_party:lombok_plugin",
        "@aggregation//:org_projectlombok_lombok",
    ],
)
