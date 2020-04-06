load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "societegenerale",
    srcs = glob(["*.java"]),
    data = ["//:agent_test_data"],
    tags = [
        "external",
        "manual",
    ],
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/fr/openbanking/societegenerale",
        "//src/integration/lib",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/configuration",
    ],
)
