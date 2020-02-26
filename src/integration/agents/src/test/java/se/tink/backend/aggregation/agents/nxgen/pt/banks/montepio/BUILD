load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "montepio",
    srcs = glob(["**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/pt/banks/montepio",
        "//src/integration/credentials:service",
        "//third_party:com_github_tomakehurst_wiremock",
        "//third_party:org_apache_httpcomponents_httpclient",
        "//third_party:org_json_json",
        "//third_party:org_mockito_mockito_core",
        "//third_party:pl_pragmatists_junitparams",
        "@aggregation//:com_google_guava_guava",
    ],
)
