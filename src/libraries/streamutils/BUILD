load("@rules_java//java:defs.bzl", "java_library")
load("//tools/bzl:junit.bzl", "junit_test")

java_library(
    name = "streamutils",
    srcs = glob(["src/main/**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
    ],
)

junit_test(
    name = "streamutils_test",
    srcs = glob(["src/test/**/*.java"]),
    deps = [
        "//src/libraries/streamutils",
        "//third_party:org_assertj_assertj_core",
    ],
)
