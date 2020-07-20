load("//tools/bzl:junit.bzl", "junit_test")

java_library(
    name = "models",
    srcs = glob(
        ["*.java"],
        exclude = ["*Test.java"],
    ),
    visibility = ["//visibility:public"],
    deps = [
        "//third_party:com_fasterxml_jackson_core_jackson_annotations",
        "//third_party:com_fasterxml_jackson_core_jackson_databind",
    ],
)

junit_test(
    name = "models-test",
    srcs = glob(["*Test.java"]),
    deps = [
        ":models",
        "@maven//:org_assertj_assertj_core",
    ],
)
