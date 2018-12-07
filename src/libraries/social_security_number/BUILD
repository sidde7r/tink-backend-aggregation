load("//tools/bzl:junit.bzl", "junit_test")

java_library(
    name = "social-security-number",
    srcs = glob(["src/main/**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/libraries/demo_credentials:demo-credentials",
        "//src/libraries/date",
        "//src/libraries/log:log",
        "//third_party:com_google_guava_guava",
        "//third_party:commons_codec_commons_codec",
        "//third_party:org_apache_commons_commons_lang3",
    ],
)



junit_test(
    name = "social-security-number-test",
    srcs = glob(["src/test/**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        ":social-security-number",

        "//third_party:com_google_guava_guava",
        "//third_party:junit_junit",
        "//third_party:org_assertj_assertj_core",
    ],
)
