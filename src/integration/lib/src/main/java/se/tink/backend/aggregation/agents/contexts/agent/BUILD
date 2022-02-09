load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "agent",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    runtime_deps = [
        "//po:i18n_aggregation",
    ],
    deps = [
        "//src/integration/credentials/src/main/java/se/tink/libraries/credentials/service",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/contexts",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/summary/refresh",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/api",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/configuration/agentsservice",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/eidasidentity/identity",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/logmasker",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/controllers/configuration/iface",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/log/executor",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/raw_data_events",
        "//src/libraries/agent-storage",
        "//src/libraries/interaction_counter",
        "//src/libraries/interaction_counter/local",
        "//src/tink_backend_libs:har_logger",
        "//third_party:com_google_guava_guava",
        "@tink_backend//src/libraries/metrics/core/src/main/java/se/tink/libraries/metrics/registry",
        "@tink_backend//src/libraries/unleash",
    ],
)
