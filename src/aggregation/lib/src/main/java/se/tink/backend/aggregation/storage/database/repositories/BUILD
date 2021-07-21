load("@rules_java//java:defs.bzl", "java_library")
load("//tools/bzl:junit.bzl", "junit_test")

java_library(
    name = "repositories",
    srcs = glob(["*.java"]),
    data = [
    ],
    visibility = ["//visibility:public"],
    deps = [
        "//src/aggregation/lib/src/main/java/se/tink/backend/aggregation/storage/database/models",
        "//third_party:com_amazonaws_aws_java_sdk_s3",
        "//third_party:com_google_guava_guava",
        "//third_party:com_google_inject_guice",
        "//third_party:javax_validation_validation_api",
        "//third_party:org_hibernate_hibernate_annotations",
        "//third_party:org_slf4j_slf4j_api",
        "//third_party:org_springframework_data_spring_data_jpa",
    ],
)
