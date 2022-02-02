load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "validation",
    srcs = glob(["**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/payment:rpc",
    ],
)
