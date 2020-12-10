load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "customsupplemental",
    srcs = glob(["*.java"]),
    data = [
        "//:agent_test_data",
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/demo/banks/customsupplemental/resources",
    ],
    tags = [
        "external",
        "manual",
    ],
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/demo/banks/customsupplemental",
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework",
    ],
)
