load("//tools/bzl:junit_old.bzl", "junit_test")

junit_test(
    name = "isp",
    srcs = glob(
        ["**/*.java"],
        exclude = ["agent/**/*.java"],
    ),
    data = ["//:agent_test_data"],
    tags = [
    ],
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/it/banks/isp",
        "//third_party:org_assertj_assertj_core",
    ],
)

junit_test(
    name = "isp_agent",
    srcs = glob(["agent/**/*.java"]),
    data = [
        "//data:aggregation_test",
        "//data:cryptography_test",
        "//data:provider_configurations",
        "//etc:development",
    ],
    tags = [
        "external",
        "manual",
    ],
    visibility = ["//visibility:public"],
    runtime_deps = ["//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/it/banks/isp"],
    deps = [
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework",
        "//third_party:org_assertj_assertj_core",
    ],
)
