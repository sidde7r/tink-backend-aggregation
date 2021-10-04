load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "fetcher",
    srcs = glob(["**/*.java"]),
    data = ["//:agent_test_data"],
    tags = [
        "external",
        "manual",
    ],
    visibility = ["//visibility:public"],
    runtime_deps = ["//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/se/openbanking/skandia"],
    deps = [
        "//src/aggregation/lib/src/main/java/se/tink/backend/aggregation/agents_platform/agents_framework",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/se/openbanking/skandia",
        "//src/integration/amount",
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/core/account",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework",
        "//src/integration/models",
        "//src/integration/payment:rpc",
        "//src/integration/transfer:enums",
        "//src/integration/transfer:rpc",
        "//src/libraries/serialization",
        "//third_party:org_assertj_assertj_core",
        "//third_party:org_mockito_mockito_core",
        "@tink_backend//src/libraries/account_identifier",
    ],
)
