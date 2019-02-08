java_library(
    name = "queue_sqs",
    srcs = glob(["src/main/**/*.java"]),
    resources = glob(["src/main/resources/**"]),
    visibility = ["//visibility:public"],
    deps = [
        "//third_party:org_slf4j_slf4j_api",
        "//src/libraries/metrics",
        "//src/libraries/net",
        "//src/libraries/queue_lib",
        "//third_party:com_amazonaws_aws_java_sdk_sqs",
        "//third_party:com_fasterxml_jackson_core_jackson_annotations",
        "//third_party:com_google_guava_guava",
        "//third_party:com_google_inject_guice",
        "//third_party:io_dropwizard_dropwizard_core",
        "//third_party:org_apache_httpcomponents_httpclient",
    ],
)
