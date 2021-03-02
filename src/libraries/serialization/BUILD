load("//tools/bzl:junit.bzl", "junit_test")

java_library(
    name = "serialization",
    srcs = glob(["src/main/**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//third_party:commons_codec_commons_codec",
        "//third_party:org_apache_commons_commons_lang3",
        "//third_party:org_slf4j_slf4j_api",
        "@aggregation//:com_fasterxml_jackson_core_jackson_core",
        "@aggregation//:com_fasterxml_jackson_core_jackson_databind",
        "@aggregation//:com_fasterxml_jackson_dataformat_jackson_dataformat_smile",
        "@aggregation//:com_fasterxml_jackson_datatype_jackson_datatype_jsr310",
        "@aggregation//:com_google_guava_guava",
    ],
)

junit_test(
    name = "serialization_test",
    srcs = glob(
        ["src/test/**/*.java"],
    ),
    visibility = ["//visibility:public"],
    deps = [
        "//src/libraries/serialization",
        "//third_party:org_assertj_assertj_core",
        "@aggregation//:com_google_guava_guava",
    ],
)
