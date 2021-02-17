load("//tools/bzl:junit.bzl", "junit_test")

java_library(
    name = "request_tracing",
    srcs = glob(["src/main/**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//third_party:commons_codec_commons_codec",
        "//third_party:org_slf4j_slf4j_api",
        "@aggregation//:com_google_guava_guava",
    ],
)

junit_test(
    name = "request_tracing_test",
    srcs = glob(["src/test/**/*.java"]),
    deps = [
        "//src/libraries/request_tracing",
        "//third_party:ch_qos_logback_logback_classic",
        "//third_party:junit_junit",
        "//third_party:org_assertj_assertj_core",
        "//third_party:org_slf4j_slf4j_api",
        "@aggregation//:com_google_guava_guava",
    ],
)
