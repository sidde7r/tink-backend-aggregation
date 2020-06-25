load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "sbab",
    srcs = glob(["**/*.java"]),
    data = [
        "//:agent_test_data",
    ],
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/se/banks/sbab",
        "//src/integration/amount",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen",
        "//src/libraries/serialization",
        "//third_party:org_assertj_assertj_core",
    ],
)
