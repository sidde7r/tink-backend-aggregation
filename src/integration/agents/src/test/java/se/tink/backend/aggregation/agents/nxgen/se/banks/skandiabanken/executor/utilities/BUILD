load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "utilities",
    srcs = glob(["*.java"]),
    data = [],
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/se/banks/skandiabanken",
        "//src/libraries/date",
        "//third_party:org_assertj_assertj_core",
    ],
)
