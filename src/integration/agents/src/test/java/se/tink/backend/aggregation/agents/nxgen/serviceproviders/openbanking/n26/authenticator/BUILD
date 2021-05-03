load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "authenticator",
    srcs = glob(["**/*.java"]),
    data = ["//:agent_test_data"],
    visibility = ["//visibility:public"],
    deps = [
        "//src/aggregation/lib/src/main/java/se/tink/backend/aggregation/agents_platform/agents_framework",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/n26",
        "//src/integration/credentials:service",
        "//src/integration/lib",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/utils/berlingroup",
        "//third_party:com_sun_jersey_jersey_core",
        "//third_party:org_mockito_mockito_core",
    ],
)
