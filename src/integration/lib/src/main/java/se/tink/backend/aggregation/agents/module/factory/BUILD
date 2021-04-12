java_library(
    name = "factory",
    srcs = glob(
        ["*.java"],
    ),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/credentials:service",
        "//src/integration/lib",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/agentfactory",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/contexts/agent",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/module/agentclass",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/module/loader",
        "//third_party:com_google_guava_guava",
        "//third_party:com_google_inject_guice",
        "@tink_backend//src/libraries/events:event_submitter_api",
        "@tink_backend//src/libraries/events:event_submitter_batching",
        "@tink_backend//src/libraries/events:event_submitter_configuration",
        "@tink_backend//src/libraries/events:event_submitter_mock",
        "@tink_backend//src/libraries/events:event_submitter_module",
    ],
)
