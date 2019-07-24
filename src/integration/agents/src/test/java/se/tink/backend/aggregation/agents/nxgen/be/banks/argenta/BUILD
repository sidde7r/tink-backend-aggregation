load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "argenta",
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
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/be/banks/argenta",
        "//src/integration/api",
        "//src/integration/lib",
        "//src/integration/lib:lib_test",
        "//src/libraries/amount",
        "//third_party:com_fasterxml_jackson_core_jackson_databind",
    ],
)
