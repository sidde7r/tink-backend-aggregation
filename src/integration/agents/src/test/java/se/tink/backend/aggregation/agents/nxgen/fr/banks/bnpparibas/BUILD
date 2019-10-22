load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "bnpparibas",
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
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/fr/banks/bnpparibas",
        "//src/integration/api",
        "//src/integration/lib:lib_test",
    ],
)
