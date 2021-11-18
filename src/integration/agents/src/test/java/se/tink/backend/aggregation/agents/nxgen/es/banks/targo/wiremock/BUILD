load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "wiremock",
    srcs = glob(["*.java"]),
    data = [
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/es/banks/targo/wiremock/resources",
    ],
    visibility = ["//visibility:public"],
    runtime_deps = ["//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/es/banks/targo"],
    deps = [
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/credentials:service",
        "//src/integration/enums",
        "//src/integration/lib",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework/assertions",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework/compositeagenttest/wiremockrefresh",
        "//third_party:com_google_protobuf_protobuf_java",
        "//third_party:org_assertj_assertj_core",
        "@com_tink_api_grpc//event_producer_service:event_producer_service_java_proto",
        "@com_tink_api_grpc//event_producer_service/events:events_java_proto",
        "@tink_backend//src/libraries/protobuf_type_utils",
    ],
)
