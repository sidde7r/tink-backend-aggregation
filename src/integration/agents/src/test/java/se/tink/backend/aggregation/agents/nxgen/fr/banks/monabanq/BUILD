load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "monabanq",
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
        "//src/integration/agents",
        "//src/integration/api",
        "//src/integration/lib:lib_test",
        "//src/integration/lib",
    ],
)
