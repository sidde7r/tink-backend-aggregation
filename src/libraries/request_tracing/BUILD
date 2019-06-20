load("//tools/bzl:junit.bzl", "junit_test")
load("//tools/bzl:maven_artifact.bzl", "java_library_maven")

java_library_maven(
    name = "request_tracing",
    srcs = glob(["src/main/**/*.java"]),
    groupId = "se.tink.libraries.requesttracing",
    visibility = ["//visibility:public"],
    deps = [
        "@maven//:com_google_guava_guava",
        "@maven//:commons_codec_commons_codec",
        "@maven//:org_slf4j_slf4j_api",
    ],
)

junit_test(
    name = "request_tracing_test",
    srcs = glob(["src/test/**/*.java"]),
    deps = [
        ":request_tracing",
        "@maven//:ch_qos_logback_logback_classic",
        "@maven//:com_google_guava_guava",
        "@maven//:junit_junit",
        "@maven//:org_assertj_assertj_core",
    ],
)
