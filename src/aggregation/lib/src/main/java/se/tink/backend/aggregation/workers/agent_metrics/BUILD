load("//tools/bzl:junit.bzl", "junit_test")

java_library(
    name = "agent_metrics",
    srcs = glob(["**/*.java"]),
    data = [
    ],
    visibility = ["//visibility:public"],
    runtime_deps = [
    ],
    deps = [
        "//src/aggregation/lib/src/main/java/se/tink/backend/aggregation/configuration",
        "//src/aggregation/lib/src/main/java/se/tink/backend/aggregation/workers/context",
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/libraries/metrics:core",
        "//src/libraries/metrics:counters",
        "//src/libraries/metrics:registry",
        "//third_party:com_google_guava_guava",
    ],
)
