java_library(
    name = "agent",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    runtime_deps = [
        "//po:i18n",
    ],
    deps = [
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/contexts",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/api",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/configuration/agentsservice",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/logmasker",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/controllers/configuration/iface",
        "//src/libraries/interaction_counter",
        "//src/libraries/interaction_counter/local",
        "//third_party:com_google_guava_guava",
        "@tink_backend//src/libraries/metrics/core/src/main/java/se/tink/libraries/metrics/registry",
    ],
)
