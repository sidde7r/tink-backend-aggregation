load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "apache",
    srcs = glob(["*.java"]),
    deps = [
        "//src/libraries/simple_http_server",
        "@aggregation//:com_fasterxml_jackson_core_jackson_annotations",
        "@aggregation//:com_google_guava_guava",
        "@aggregation//:org_apache_httpcomponents_httpclient",
        "@aggregation//:org_apache_httpcomponents_httpcore",
        "@aggregation//:org_yaml_snakeyaml",
    ],
)
