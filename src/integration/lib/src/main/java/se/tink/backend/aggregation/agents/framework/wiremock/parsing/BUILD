load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "parsing",
    srcs = glob([
        "*.java",
    ]),
    data = [],
    visibility = ["//visibility:public"],
    deps = [
        "//third_party:com_github_tomakehurst_wiremock",
        "//third_party:com_sun_jersey_jersey_core",
        "//third_party:org_apache_httpcomponents_httpclient",
        "//third_party:org_apache_httpcomponents_httpcore",
        "@aggregation//:com_fasterxml_jackson_core_jackson_core",
        "@aggregation//:com_fasterxml_jackson_core_jackson_databind",
        "@aggregation//:com_google_guava_guava",
    ],
)
