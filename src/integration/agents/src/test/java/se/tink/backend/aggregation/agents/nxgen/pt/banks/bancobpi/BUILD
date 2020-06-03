load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "bancobpi",
    srcs = glob(["**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/common",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/pt/banks/bancobpi",
        "//src/integration/credentials:service",
        "//src/integration/lib",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/exceptions/bankservice",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/client",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/filter/filterable/request",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/response",
        "//third_party:com_github_tomakehurst_wiremock",
        "//third_party:com_sun_jersey_jersey_core",
        "//third_party:org_apache_httpcomponents_httpclient",
        "//third_party:org_json_json",
        "//third_party:org_mockito_mockito_core",
        "//third_party:pl_pragmatists_junitparams",
        "@aggregation//:com_google_guava_guava",
    ],
)
