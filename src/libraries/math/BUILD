load("//tools/bzl:junit.bzl", "junit_test")

java_library(
    name = "math",
    srcs = glob(["src/main/**/*.java"]),
    visibility = ["//visibility:public"],
)

junit_test(
    name = "math-test",
    srcs = glob(["src/test/**/*.java"]),
    deps = [
        ":math",
        
        "//third_party:junit_junit",
        "//third_party:org_assertj_assertj_core",
    ],
)
