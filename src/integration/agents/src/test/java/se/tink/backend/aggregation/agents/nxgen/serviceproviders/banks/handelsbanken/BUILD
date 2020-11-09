load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "handelsbanken",
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
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/fi/banks/handelsbanken",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/se/banks/handelsbanken",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/banks/handelsbanken",
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/storage",
        "//third_party:com_fasterxml_jackson_core_jackson_core",
        "//third_party:com_github_tomakehurst_wiremock",
        "//third_party:commons_io_commons_io",
        "//third_party:org_assertj_assertj_core",
        "//third_party:org_mockito_mockito_core",
    ],
)
