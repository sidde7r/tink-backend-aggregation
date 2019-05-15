load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "hvb",
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
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/de/banks/hvb",
        "//src/integration/api",
        "//src/integration/lib",
        "//src/integration/lib:lib_test",
        "//third_party:org_hamcrest_hamcrest_core",
        "//third_party:org_hamcrest_hamcrest_library",
    ],
)
