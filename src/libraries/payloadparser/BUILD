load("@rules_java//java:defs.bzl", "java_library")
load("//tools/bzl:junit.bzl", "junit_test")

java_library(
    name = "payloadparser",
    srcs = glob(["src/main/**/*.java"]),
    visibility = ["//visibility:public"],
)

junit_test(
    name = "payloadparser_test",
    srcs = glob(
        ["src/test/**/*.java"],
    ),
    visibility = ["//visibility:public"],
    deps = [
        "//src/libraries/payloadparser",
        "//third_party:junit_junit",
        "//third_party:org_assertj_assertj_core",
    ],
)
