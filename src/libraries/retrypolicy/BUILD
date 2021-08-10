load("@rules_java//java:defs.bzl", "java_library")
load("//tools/bzl:junit.bzl", "junit_test")

java_library(
    name = "retrypolicy",
    srcs = glob(["src/main/**/*.java"]),
    visibility = ["//visibility:public"],
)

junit_test(
    name = "retrypolicy_test",
    srcs = glob(
        ["src/test/**/*.java"],
    ),
    visibility = ["//visibility:public"],
    deps = [
        "//src/libraries/retrypolicy",
        "//third_party:junit_junit",
        "//third_party:org_assertj_assertj_core",
        "//third_party:org_mockito_mockito_core",
    ],
)
