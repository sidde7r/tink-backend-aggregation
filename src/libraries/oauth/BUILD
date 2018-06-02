load("//tools/bzl:junit.bzl", "junit_test")

java_library(
    name = "oauth",
    srcs = glob(["src/main/**/*.java"]),
    visibility = ["//visibility:public"]
)

junit_test(
    name = "oauth-test",
    srcs = glob(["src/test/**/*.java"]),
    deps = [
        "//third_party:junit_junit",
        ":oauth",
        "//third_party:org_assertj_assertj_core",
    ],
)
