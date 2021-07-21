load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "grpc-services",
    srcs = glob(["src/main/java/**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/grpc-services/proto:integration-proto",
        "//src/integration/grpc-services/proto:java_grpc",
        "//src/integration/grpc-services/proto:java_proto",
        "//src/integration/lib",  # TODO: remove this dependency
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/agent",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/agentfactory",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/agentfactory/iface",
        "//src/libraries/metrics:collection",
        "//src/libraries/metrics:configuration",
        "//src/libraries/metrics:core",
        "//src/libraries/metrics:counters",
        "//src/libraries/metrics:gauges",
        "//src/libraries/metrics:histograms",
        "//src/libraries/metrics:logbackappender",
        "//src/libraries/metrics:monitor",
        "//src/libraries/metrics:other",
        "//src/libraries/metrics:prometheus",
        "//src/libraries/metrics:registry",
        "//src/libraries/metrics:timers",  # TODO: remove this dependency
        "//third_party:com_google_inject_guice",
        "@aggregation//:com_google_guava_guava",
        "@io_grpc_grpc_java//api",
        "@io_grpc_grpc_java//stub",
    ],
)
