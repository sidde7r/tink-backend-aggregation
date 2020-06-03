load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "transaction",
    srcs = glob(["*.java"]),
    data = [
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/nl/banks/openbanking/rabobank/resources",
    ],
    tags = [],
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/nl/banks/openbanking/rabobank",
        "//src/integration/lib",
        "//src/integration/models",
        "//src/libraries/serialization",
    ],
)
