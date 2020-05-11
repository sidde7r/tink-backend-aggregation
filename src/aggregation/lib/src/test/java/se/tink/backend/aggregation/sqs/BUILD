load("@tink_backend//tools/bzl:junit_strict.bzl", "junit_test")

junit_test(
    name = "sqs",
    srcs = glob(["*.java"]),
    data = [],
    visibility = ["//visibility:public"],
    deps = ["//third_party:com_amazonaws_aws_java_sdk_sqs"],
)
