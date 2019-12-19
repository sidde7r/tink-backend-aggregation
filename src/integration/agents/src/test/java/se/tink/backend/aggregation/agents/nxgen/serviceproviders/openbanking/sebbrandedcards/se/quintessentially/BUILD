load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "quintessentially",
    srcs = glob(["*.java"]),
    data = [
        "//data:aggregation_test",
        "//data:cryptography_test",
        "//etc:development",
    ],
    tags = [
        "external",
        "manual",
    ],
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/sebbase",
    ],
)
