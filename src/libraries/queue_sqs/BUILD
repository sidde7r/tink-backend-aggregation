load("//tools/bzl:maven_artifact.bzl", "java_library_maven")

java_library_maven(
    name = "queue_sqs",
    srcs = glob(["src/main/**/*.java"]),
    groupId = "se.tink.libraries.queue.sqs",
    resources = glob(["src/main/resources/**"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/libraries/metrics",
        "//src/libraries/queue_lib",
        "//third_party:com_amazonaws_aws_java_sdk_sqs",
        "//third_party:io_dropwizard_dropwizard_core",
        "//third_party:io_dropwizard_dropwizard_lifecycle",
        "@maven//:com_amazonaws_aws_java_sdk_core",
        "@maven//:com_fasterxml_jackson_core_jackson_annotations",
        "@maven//:com_google_guava_guava",
        "@maven//:com_google_inject_guice",
        "@maven//:org_apache_httpcomponents_httpclient",
        "@maven//:org_apache_httpcomponents_httpcore",
        "@maven//:org_slf4j_slf4j_api",
    ],
)
