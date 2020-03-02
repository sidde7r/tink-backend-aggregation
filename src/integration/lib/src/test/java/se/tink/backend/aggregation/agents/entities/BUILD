load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "entities",
    srcs = glob(["*.java"]),
    data = [],
    tags = [],
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/lib",
        "@aggregation//:com_fasterxml_jackson_core_jackson_core",
        "@aggregation//:com_fasterxml_jackson_core_jackson_databind",
    ],
)
