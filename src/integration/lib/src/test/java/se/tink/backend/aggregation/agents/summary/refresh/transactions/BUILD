load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "oldest_transactions",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/amount",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/summary/refresh",
        "//src/integration/models",
        "//third_party:org_assertj_assertj_core",
    ],
)
