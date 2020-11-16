load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "agents_framework",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    runtime_deps = [
        "//src/aggregation/lib/src/main/java/se/tink/backend/aggregation/agents_platform/agents_framework",
    ],
    deps = [
        "//third_party:commons_io_commons_io",
        "//third_party:org_assertj_assertj_core",
    ],
)
