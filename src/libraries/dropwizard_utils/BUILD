load("//tools/bzl:junit.bzl", "junit_test")

java_library(
    name = "dropwizard-utils",
    srcs = glob(["src/main/**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/libraries/discovery",
        "//src/libraries/jersey_log:jersey-log",
        "//src/libraries/log",
        "//src/libraries/uuid",
        "//third_party:com_netflix_governator",
        "//third_party:io_dropwizard_dropwizard_core",
        "//third_party:io_protostuff_protostuff_api",
        "//third_party:io_protostuff_protostuff_core",
        "//third_party:io_protostuff_protostuff_runtime",
        "//third_party:io_swagger_swagger_jaxrs",
    ],
)

junit_test(
    name = "dropwizard-utils-test",
    srcs = glob(["src/test/**/*.java"]),
    deps = [
        ":dropwizard-utils",
        "//third_party:io_dropwizard_dropwizard_core",
        "//third_party:joda_time_joda_time",
        "//third_party:junit_junit",
        "//third_party:org_yaml_snakeyaml",
    ],
)
