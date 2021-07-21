load("@rules_java//java:defs.bzl", "java_library")
load("//tools/bzl:junit.bzl", "junit_test")

java_library(
    name = "iban",
    srcs = glob(["src/main/**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//third_party:com_google_guava_guava",
        "//third_party:lombok_libs",
    ],
)

junit_test(
    name = "iban_test",
    srcs = glob(["src/test/**/*.java"]),
    deps = [
        "//src/libraries/iban",
        "//third_party:junit_junit",
        "//third_party:org_assertj_assertj_core",
        "//third_party:pl_pragmatists_junitparams",
    ],
)
