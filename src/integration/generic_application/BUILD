load("//tools/bzl:junit.bzl", "junit_test")

java_library(
    name = "generic_application",
    srcs = glob(["src/main/**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/libraries/serialization",
        "@aggregation//:com_fasterxml_jackson_core_jackson_annotations",
        "@aggregation//:com_fasterxml_jackson_core_jackson_core",
        "@aggregation//:com_fasterxml_jackson_core_jackson_databind",
        "@aggregation//:com_google_guava_guava",
    ],
)

junit_test(
    name = "generic_application_test",
    srcs = glob(["src/test/**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/generic_application",
        "//third_party:junit_junit",
        "//third_party:org_assertj_assertj_core",
        "@aggregation//:com_google_guava_guava",
    ],
)
