load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "log",
    srcs = glob(["*.java"]),
    deps = [
        "//src/aggregation/lib/src/main/java/se/tink/backend/aggregation/log",
        "//third_party:ch_qos_logback_logback_classic",
        "//third_party:lombok_libs",
        "//third_party:org_assertj_assertj_core",
        "//third_party:org_mockito_mockito_core",
        "//third_party:org_slf4j_slf4j_api",
    ],
)
