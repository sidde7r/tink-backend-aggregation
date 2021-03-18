load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "manual",
    srcs = glob(["**/*.java"]),
    data = ["//:agent_test_data"],
    tags = [
        "external",
        "manual",
    ],
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/legacy/banks/sbab",
        "//src/integration/amount",
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/credentials:service",
        "//src/integration/lib",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework/context",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework/legacy",
        "//src/integration/models",
        "//src/integration/transfer:enums",
        "//src/integration/transfer:rpc",
        "//src/libraries/date",
        "//third_party:org_assertj_assertj_core",
        "//third_party:org_jsoup_jsoup",
        "@tink_backend//src/libraries/account_identifier",
        "@tink_backend//src/libraries/account_identifier/src/test/java/se/tink/libraries/account/identifiers/account",
    ],
)
