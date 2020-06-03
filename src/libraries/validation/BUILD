load("//tools/bzl:junit.bzl", "junit_test")

java_library(
    name = "validation",
    srcs = glob(["src/main/**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "@aggregation//:com_google_guava_guava",
    ],
)

junit_test(
    name = "validation_test",
    srcs = glob(["src/test/**/*.java"]),
    deps = [
        "//src/libraries/validation",
        "//third_party:junit_junit",
        "//third_party:org_assertj_assertj_core",
        "@aggregation//:com_google_guava_guava",
    ],
)
