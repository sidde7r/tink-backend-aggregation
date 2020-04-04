load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "finnairmastercard",
    srcs = glob(["*.java"]),
    data = ["//:agent_test_data"],
    tags = [
        "external",
        "manual",
    ],
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/sebbase",
    ],
)
