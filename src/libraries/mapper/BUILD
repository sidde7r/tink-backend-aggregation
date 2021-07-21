load("@rules_java//java:defs.bzl", "java_library")
load("//tools/bzl:junit.bzl", "junit_test")

java_library(
    name = "mapper",
    srcs = glob(["src/main/**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//third_party:lombok_libs",
        "//third_party:org_apache_commons_commons_collections4",
        "@aggregation//:com_google_guava_guava",
    ],
)

junit_test(
    name = "iban_test",
    srcs = glob(["src/test/**/*.java"]),
    deps = [
        "//src/libraries/mapper",
        "//third_party:junit_junit",
        "//third_party:org_assertj_assertj_core",
    ],
)
