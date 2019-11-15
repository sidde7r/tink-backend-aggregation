load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "sqs",
    srcs = glob(["*.java"]),
    data = [],
    visibility = ["//visibility:public"],
    deps = [
        "//src/aggregation/lib",
        "//third_party:com_amazonaws_aws_java_sdk_sqs",
    ],
)
