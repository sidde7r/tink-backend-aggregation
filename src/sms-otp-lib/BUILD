load("//tools/bzl:junit.bzl", "junit_test")

java_library(
    name = "sms-otp-lib",
    srcs = glob(["src/main/**/*.java"]),
    deps = [
        "//src/sms-gateway-lib",
        "//src/libraries/i18n",
        "//src/libraries/cassandra-interfaces",
        "//src/libraries/versioning:versioning",
        "//src/libraries/uuid:uuid",
        
        "//third_party:com_fasterxml_jackson_core_jackson_annotations",
        "//third_party:com_google_guava_guava",
        "//third_party:com_google_inject_guice",
        "//third_party:io_protostuff_protostuff_api",
        "//third_party:io_swagger_swagger_annotations",
        "//third_party:org_assertj_assertj_core",
        "//third_party:org_modelmapper_modelmapper",
        "//third_party:org_springframework_data_spring_data_cassandra",
        "//third_party:joda_time_joda_time",
    ],
    visibility = ["//visibility:public"],
)

junit_test(
    name = "sms-otp-lib-test",
    srcs = glob(["src/test/**/*.java"]),
    deps = [
        ":sms-otp-lib",
        "//src/sms-gateway-lib",

        "//third_party:com_google_guava_guava",
        "//third_party:org_assertj_assertj_core",
        "//third_party:joda_time_joda_time",
        "//third_party:com_fasterxml_jackson_core_jackson_databind",
        "//third_party:com_fasterxml_jackson_core_jackson_core",
    ],
)
