load("//tools/bzl:junit.bzl", "junit_test")

java_library(
    name = "customer_info",
    srcs = glob(["src/main/**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//third_party:org_slf4j_slf4j_api",

        # Guava
        "//third_party:com_google_guava_guava",

        # Jackson
        "//third_party:com_fasterxml_jackson_core_jackson_databind",
    ],
)

junit_test(
    name = "customer_info_test",
    srcs = glob(["src/test/**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/libraries/customer_info",
        "//third_party:com_google_guava_guava",
        "//third_party:junit_junit",
        "//third_party:org_assertj_assertj_core",
    ],
)
