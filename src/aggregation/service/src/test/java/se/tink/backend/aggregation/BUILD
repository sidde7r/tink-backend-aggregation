load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "aggregation",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    runtime_deps = [
        # Intentionally drag in transitive dependency closure of aggregation service to classpath
        "//src/aggregation/service",
    ],
    deps = [
        "//third_party:commons_io_commons_io",
        "//third_party:org_assertj_assertj_core",
    ],
)
