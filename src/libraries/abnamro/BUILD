load("//tools/bzl:junit.bzl", "junit_test")

java_library(
    name = "abnamro",
    srcs = glob(["src/main/**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/libraries/account:enums",
        "//src/libraries/account:rpc",
        "//src/libraries/credentials:rpc",
        "//src/libraries/date",
        "//src/libraries/enums",
        "//src/libraries/log",
        "//src/libraries/metrics",
        "//src/libraries/net",
        "//src/libraries/serialization",
        "//src/libraries/strings",
        "//src/libraries/user:rpc",
        "//third_party:com_fasterxml_jackson_core_jackson_annotations",
        "//third_party:com_google_guava_guava",
        "//third_party:com_google_http_client_google_http_client",
        "//third_party:com_sun_jersey_jersey_client",
        "//third_party:io_protostuff_protostuff_api",
        "//third_party:joda_time_joda_time",
        "//third_party:org_apache_commons_commons_collections4",
        "//third_party:org_apache_commons_commons_lang3",
    ],
)

junit_test(
    name = "abnamro_test",
    srcs = glob(["src/test/**/*.java"]),
    deps = [
        "//src/libraries/abnamro",
        "//src/libraries/account:rpc",
        "//src/libraries/user:rpc",
        "//third_party:com_google_guava_guava",
        "//third_party:joda_time_joda_time",
        "//third_party:junit_junit",
        "//third_party:org_assertj_assertj_core",
        "//third_party:pl_pragmatists_junitparams",
    ],
)
