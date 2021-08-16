load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "refresh",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/credentials/src/main/java/se/tink/libraries/credentials/service",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/summary/refresh",
        "//third_party:junit_junit",
        "//third_party:org_assertj_assertj_core",
    ],
)
