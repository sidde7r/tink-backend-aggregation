load("//tools/bzl:junit.bzl", "junit_test")

java_library(
    name = "generic-application",
    srcs = glob(["src/main/**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/libraries/serialization:serialization",
        
        "//third_party:com_google_guava_guava",
        "//third_party:com_fasterxml_jackson_core_jackson_databind",
    ],
)

junit_test(
    name = "generic-application-test",
    srcs = glob(["src/test/**/*.java"]),
    deps = [
        ":generic-application",

        "//third_party:junit_junit",
        "//third_party:org_assertj_assertj_core",
        "//third_party:com_google_guava_guava",
    ],
)
