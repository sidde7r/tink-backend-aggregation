load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "resursbank",
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
    runtime_deps = ["//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/se/openbanking/resursbank"],
    deps = [
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework",
    ],
)
