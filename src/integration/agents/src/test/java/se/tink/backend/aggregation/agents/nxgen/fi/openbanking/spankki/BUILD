load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "spankki",
    srcs = glob(["*.java"]),
    data = [
        "//data:aggregation_test",
        "//etc:development",
    ],
    tags = [
        "external",
        "manual",
    ],
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/amount",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework",
        "//src/integration/payment:rpc",
        "//third_party:org_mockito_mockito_core",
    ],
)
