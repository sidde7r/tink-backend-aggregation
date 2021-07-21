load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "parser",
    srcs = glob(["**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//third_party:org_bouncycastle_bcpkix_jdk15on",
    ],
)
