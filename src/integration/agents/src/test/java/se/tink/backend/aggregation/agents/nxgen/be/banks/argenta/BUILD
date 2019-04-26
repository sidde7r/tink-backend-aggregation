load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "argenta",
    srcs = glob(["**/*.java"]),
    data = [
        "//data:provider_configuration_test",
        "//etc:development",
    ],
    tags = [
        "external",
        "manual",
    ],
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/be/banks/argenta",
        "//src/integration/api",
        "//src/integration/lib",
        "//src/integration/lib:lib_test",
        "//src/libraries/amount",
        "@com_fasterxml_jackson_core_jackson_databind//jar",
    ],
)
