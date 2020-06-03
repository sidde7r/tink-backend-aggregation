load("@tink_backend//tools/bzl:junit_strict.bzl", "junit_test")

java_library(
    name = "ratelimit",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/log",
        "//src/libraries/concurrency",
        "//third_party:com_google_guava_guava",
        "//third_party:io_dropwizard_dropwizard_lifecycle",
        "@tink_backend//src/libraries/metrics/core/src/main/java/se/tink/libraries/metrics/core",
        "@tink_backend//src/libraries/metrics/core/src/main/java/se/tink/libraries/metrics/registry",
        "@tink_backend//src/libraries/metrics/core/src/main/java/se/tink/libraries/metrics/types/histograms",
        "@tink_backend//src/libraries/tracing",
    ],
)
