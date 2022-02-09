load("@rules_java//java:defs.bzl", "java_library")
load("//tools/bzl:junit.bzl", "junit_test")

java_library(
    name = "module",
    srcs = glob(["*.java"]),
    data = [],
    visibility = ["//visibility:public"],
    deps = [
        "//src/agent_sdk/compatibility_layers/aggregation_service/src/modules",
        "//src/agent_sdk/compatibility_layers/aggregation_service/test/modules",
        "//src/integration/credentials:service",
        "//src/integration/lib",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/agentfactory/impl",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/contexts/agent",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/framework/wiremock/configuration",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/framework/wiremock/configuration/provider/socket",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/module/factory",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/raw_data_events",
        "//third_party:com_google_inject_guice",
        "//third_party:com_google_protobuf_protobuf_java",
        "@aggregation//:com_google_guava_guava",
        "@com_tink_api_grpc//event_producer_service:event_producer_service_java_proto",
        "@com_tink_api_grpc//event_producer_service/events:events_java_proto",
        "@tink_backend//src/libraries/event_producer_service_client",
        "@tink_backend//src/libraries/events:event_submitter_api",
        "@tink_backend//src/libraries/events:event_submitter_batching",
        "@tink_backend//src/libraries/events:event_submitter_configuration",
        "@tink_backend//src/libraries/events:event_submitter_mock",
        "@tink_backend//src/libraries/events:event_submitter_module",
        "@tink_backend//src/libraries/protobuf_type_utils",
        "@tink_backend//src/libraries/unleash",
    ],
)
