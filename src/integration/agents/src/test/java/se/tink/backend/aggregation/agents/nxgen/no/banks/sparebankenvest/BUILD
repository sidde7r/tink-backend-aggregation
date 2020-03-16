load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "sparebankenvest",
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
        "//src/integration/api",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework",
        "//third_party:org_assertj_assertj_core",
        "//third_party:org_mockito_mockito_core",
    ],
)
