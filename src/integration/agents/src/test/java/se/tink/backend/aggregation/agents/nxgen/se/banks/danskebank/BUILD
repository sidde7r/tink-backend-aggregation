load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "danskebank",
    srcs = glob(["**/*.java"]),
    data = [
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/se/banks/danskebank/mock/resources",
    ],
    visibility = ["//visibility:public"],
    runtime_deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/se/banks/danskebank",
    ],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/se/banks/danskebank",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/banks/danskebank",
        "//src/integration/credentials:service",
        "//src/integration/lib",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/response",
        "//src/libraries/serialization",
        "//third_party:com_github_tomakehurst_wiremock",
        "//third_party:org_mockito_mockito_core",
    ],
)
