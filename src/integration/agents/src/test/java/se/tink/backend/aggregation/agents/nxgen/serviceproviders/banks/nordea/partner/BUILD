load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "partner",
    srcs = glob(["**/*.java"]),
    data = [
        "//data:agents",
        "//data:aggregation_test",
        "//etc:development",
    ],
    tags = [
        "external",
        "manual",
    ],
    visibility = ["//visibility:public"],
    runtime_deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/se/banks/nordea/partner",
    ],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/banks/nordea/partner",
        "//src/integration/amount",
        "//src/integration/api",
        "//src/integration/credentials:service",
        "//src/integration/identity_data",
        "//src/integration/lib",
        "//src/integration/lib:lib_test",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/configuration",
        "//src/integration/transfer:enums",
        "//src/integration/transfer:rpc",
        "//src/integration/user:rpc",
        "//src/libraries/account_identifier",
        "//src/libraries/serialization",
        "//third_party:com_github_tomakehurst_wiremock",
        "//third_party:org_mockito_mockito_core",
    ],
)
