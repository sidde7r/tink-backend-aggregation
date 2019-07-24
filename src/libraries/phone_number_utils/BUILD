load("//tools/bzl:junit.bzl", "junit_test")

java_library(
    name = "phone_number_utils",
    srcs = glob(["src/main/**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//third_party:com_googlecode_libphonenumber_libphonenumber",
    ],
)

junit_test(
    name = "phone_number_utils_test",
    srcs = glob(["src/test/**/*.java"]),
    deps = [
        "//src/libraries/phone_number_utils",
        "//third_party:junit_junit",
        "//third_party:org_assertj_assertj_core",
    ],
)
