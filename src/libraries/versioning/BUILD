load("//tools/bzl:junit.bzl", "junit_test")

java_library(
    name = "versioning",
    srcs = glob(["src/main/**/*.java"]),
    visibility = ["//visibility:public"]
)

junit_test(
    name = "versioning-test",
    srcs = glob(["src/test/**/*.java"]),
    deps = [
        ":versioning",
    ],
)
