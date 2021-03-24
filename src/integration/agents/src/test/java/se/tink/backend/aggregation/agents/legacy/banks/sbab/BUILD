load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "sbab",
    srcs = glob(["**/*.java"]),
    data = [
        "//:agent_test_data",
        "//data:agent_unit_tests",
    ],
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/legacy/banks/sbab",
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/credentials:service",
        "//src/integration/lib",
        "//src/integration/models",
        "//src/libraries/date",
        "//third_party:org_assertj_assertj_core",
        "//third_party:org_jsoup_jsoup",
        "@tink_backend//src/libraries/account_identifier",
    ],
)
