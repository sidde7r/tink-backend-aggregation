load("//tools/bzl:junit_old.bzl", "junit_test")

junit_test(
    name = "agent_metrics",
    srcs = glob(["**/*.java"]),
    deps = [
        "//src/aggregation/api",
        "//src/aggregation/lib/src/main/java/se/tink/backend/aggregation/configuration",
        "//src/aggregation/lib/src/main/java/se/tink/backend/aggregation/workers/agent_metrics",
        "//src/aggregation/lib/src/main/java/se/tink/backend/aggregation/workers/context",
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/signableoperation/src/main/java/se/tink/libraries/signableoperation/enums",
        "//src/libraries/metrics:collection",
        "//src/libraries/metrics:core",
        "//src/libraries/metrics:counters",
        "//src/libraries/metrics:registry",
        "//third_party:com_google_guava_guava",
        "//third_party:org_mockito_mockito_core",
        "@tink_backend//src/aggregation-controller/api/src/main/java/se/tink/backend/aggregationcontroller/v1/rpc/enums",
    ],
)
