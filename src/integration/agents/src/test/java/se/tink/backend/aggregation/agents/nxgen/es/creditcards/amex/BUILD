load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "amex",
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
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/es/creditcards/amex",
        "//src/integration/api",
        "//src/integration/lib:lib_test",
        "//src/libraries/credentials:service",
    ],
)
