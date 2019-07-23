load("//tools/bzl:junit.bzl", "junit_test")

java_library(
    name = "uuid",
    srcs = glob(["src/main/**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//third_party:com_fasterxml_jackson_core_jackson_databind",
        "//third_party:com_google_guava_guava",
    ],
)

junit_test(
    name = "uuid_test",
    srcs = glob(["src/test/**/*.java"]),
    deps = [
        "//src/libraries/uuid",
        "//third_party:org_assertj_assertj_core",
    ],
)
