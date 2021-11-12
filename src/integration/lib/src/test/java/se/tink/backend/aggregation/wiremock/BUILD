load("@rules_java//java:defs.bzl", "java_library")
load("//tools/bzl:junit.bzl", "junit_test")

java_library(
    name = "wiremock",
    srcs = glob(["**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/lib",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/framework/wiremock",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/logmasker",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/client",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/raw_data_events",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/fakelogmasker",
        "//third_party:com_github_tomakehurst_wiremock",
        "//third_party:junit_junit",
        "//third_party:org_apache_httpcomponents_httpclient",
        "@aggregation//:com_google_guava_guava",
    ],
)
