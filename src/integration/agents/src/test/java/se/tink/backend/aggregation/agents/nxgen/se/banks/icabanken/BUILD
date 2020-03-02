load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "icabanken",
    srcs = glob(["**/*.java"]),
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
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/se/banks/icabanken",
        "//src/integration/api",
        "//src/integration/credentials:service",
        "//src/integration/lib",
        "//src/integration/lib:lib_test",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework",
        "@aggregation//:com_fasterxml_jackson_core_jackson_databind",
    ],
)
