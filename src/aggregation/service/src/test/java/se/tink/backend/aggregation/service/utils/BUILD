load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "utils",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    runtime_deps = [
        "//third_party:org_apache_httpcomponents_httpclient",
    ],
    deps = [
        "//third_party:lombok_plugin",
        "@aggregation//:com_fasterxml_jackson_core_jackson_core",
        "@aggregation//:com_fasterxml_jackson_core_jackson_databind",
        "@aggregation//:com_google_guava_guava",
        "@aggregation//:commons_io_commons_io",
        "@aggregation//:org_apache_httpcomponents_httpclient",
        "@aggregation//:org_hamcrest_hamcrest_core",
        "@aggregation//:org_hamcrest_hamcrest_library",
        "@aggregation//:org_projectlombok_lombok",
        "@aggregation//:org_springframework_boot_spring_boot_test",
        "@aggregation//:org_springframework_spring_test",
        "@aggregation//:org_springframework_spring_web",
        "@aggregation//:org_testcontainers_testcontainers",
        "@org_slf4j_slf4j_api",
    ],
)
