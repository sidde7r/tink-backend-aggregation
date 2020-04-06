load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "knab",
    srcs = glob(["*.java"]),
    data = ["//:agent_test_data"],
    tags = [
        "external",
        "manual",
    ],
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/nl/openbanking/knab",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework",
    ],
)
