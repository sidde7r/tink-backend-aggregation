load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "alandsbanken",
    srcs = glob(["**/*.java"]),
    data = [
        "//data:aggregation_test",
        "//data:provider_configurations",
        "//etc:development",
    ],
    tags = [
        "external",
        "manual",
    ],
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/fi/banks/alandsbanken",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/banks/crosskey",
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/credentials:service",
        "//src/integration/lib",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/mocks",
        "//src/libraries/agent-storage",
        "//third_party:com_github_tomakehurst_wiremock",
        "//third_party:commons_io_commons_io",
        "//third_party:org_mockito_mockito_core",
    ],
)
