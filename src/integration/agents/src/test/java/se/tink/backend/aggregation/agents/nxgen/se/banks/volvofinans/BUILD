load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "volvofinans",
    srcs = glob(["**/*.java"]),
    data = [
    ],
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/se/banks/volvofinans",
        "//src/integration/credentials:service",
        "//src/integration/lib",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/exceptions/bankservice",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/response",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/storage",
        "//src/libraries/serialization",
        "//third_party:com_github_tomakehurst_wiremock",
        "//third_party:org_apache_httpcomponents_httpcore",
        "//third_party:org_mockito_mockito_core",
    ],
)
