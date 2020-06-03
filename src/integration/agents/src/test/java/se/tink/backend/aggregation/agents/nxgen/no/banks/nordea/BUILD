load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "nordea",
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
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/no/banks/nordea",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/banks/nordea/v17",
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/configuration",
        "//third_party:org_mockito_mockito_core",
    ],
)
