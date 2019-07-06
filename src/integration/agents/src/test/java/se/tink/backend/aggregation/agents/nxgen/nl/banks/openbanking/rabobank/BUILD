load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "rabobank",
    srcs = glob(["*.java"]),
    data = [
        "//data:aggregation_test",
        "//etc:development",
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/nl/banks/openbanking/rabobank/resources",
    ],
    tags = [
        "external",
        "manual",
    ],
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/nl/banks/openbanking/rabobank",
        "//src/integration/lib",
        "//src/integration/lib:lib_test",
        "//src/libraries/serialization",
    ],
)
