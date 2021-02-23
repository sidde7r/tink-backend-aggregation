load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "events",
    srcs = glob(["*.java"]),
    deps = [
        "//src/aggregation/lib/src/main/java/se/tink/backend/aggregation/events",
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/libraries/pair",
        "//src/libraries/uuid",
        "//third_party:com_google_protobuf_protobuf_java",
        "//third_party:org_mockito_mockito_core",
        "@aggregation//:com_google_guava_guava",
        "@com_tink_api_grpc//event_producer_service:event_producer_service_java_proto",
        "@com_tink_api_grpc//event_producer_service:event_producer_service_proto",
        "@com_tink_api_grpc//event_producer_service/events:events_java_proto",
        "@tink_backend//src/libraries/event_producer_service_client",
        "@tink_backend//src/libraries/events:event_submitter_api",
        "@tink_backend//src/libraries/events:event_submitter_module",
    ],
)
