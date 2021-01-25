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
        "//src/aggregation/api",
        "//src/aggregation/lib/src/main/java/se/tink/backend/aggregation/configuration",
        "//src/aggregation/lib/src/main/java/se/tink/backend/aggregation/workers/context",
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/credentials/src/main/java/se/tink/libraries/credentials/service",
        "//src/integration/signableoperation/src/main/java/se/tink/libraries/signableoperation/rpc",
        "//src/libraries/metrics:core",
        "//src/libraries/metrics:counters",
        "//src/libraries/metrics:registry",
        "//third_party:com_google_guava_guava",
        "//third_party:org_slf4j_slf4j_api",
        "@tink_backend//src/aggregation-controller/api/src/main/java/se/tink/backend/aggregationcontroller/v1/rpc/enums",
    ],
)
