load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "events",
    srcs = glob(["*.java"]),
    deps = [
        "//src/aggregation/lib",
        "//src/integration/api",
        "//src/libraries/uuid",
        "//third_party:com_google_guava_guava",
        "//third_party:com_google_protobuf_protobuf_java",
        "//third_party:org_mockito_mockito_core",
        "@com_tink_api_grpc//event_producer_service/events:events_java_proto",
        "@tink_backend//src/libraries/event_producer_service_client",
        "@tink_backend//src/libraries/event_producer_service_client/proto",
    ],
)
