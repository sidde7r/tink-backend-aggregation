load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "wiremock",
    srcs = glob(["**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/lib",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/logmasker",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/client",
        "//third_party:com_github_tomakehurst_wiremock",
        "//third_party:org_apache_httpcomponents_httpclient",
    ],
)
