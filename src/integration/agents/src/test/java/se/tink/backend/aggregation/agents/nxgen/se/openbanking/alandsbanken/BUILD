load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "alandsbanken",
    srcs = glob(["*.java"]),
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
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework",
    ],
)
