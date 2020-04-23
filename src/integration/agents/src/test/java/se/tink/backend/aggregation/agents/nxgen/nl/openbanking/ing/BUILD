load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "ing",
    srcs = glob(["**/*.java"]),
    data = [
        "//:agent_test_data",
        "//data:cryptography_test",
    ],
    tags = [
        "external",
        "manual",
    ],
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/nl/openbanking/ing",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/ingbase",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework",
    ],
)
