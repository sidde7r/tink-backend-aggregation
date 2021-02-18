load("//tools/bzl:junit.bzl", "junit_test")

java_library(
    name = "debug",
    srcs = glob(["*.java"]),
    data = [
    ],
    visibility = ["//visibility:public"],
    deps = [
        "//src/aggregation/lib/src/main/java/se/tink/backend/aggregation/configuration/models/configuration",
        "//third_party:com_amazonaws_aws_java_sdk_s3",
        "//third_party:com_amazonaws_aws_java_sdk_sqs",
        "//third_party:com_google_guava_guava",
        "//third_party:com_google_inject_guice",
        "//third_party:org_slf4j_slf4j_api",
        "@tink_backend//src/libraries/payments_legal_constraints/src/main/java/se/tink/libraries/payments_legal_constraints",
    ],
)
