load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "raiffeisen",
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
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/at/banks/raiffeisen",
        "//src/integration/api",
        "//src/integration/lib:lib_test",
    ],
)
