load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "wiremock",
    srcs = glob(["*.java"]),
    data = [
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/se/openbanking/norwegian/wiremock/resources",
    ],
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/se/openbanking/norwegian",
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/se/openbanking/norwegian/wiremock/module",
        "//src/integration/amount",
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/credentials/src/main/java/se/tink/libraries/credentials/service",
        "//src/integration/enums",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/exceptions",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/configuration/agentsservice",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework/assertions",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework/compositeagenttest/wiremockpayment",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework/compositeagenttest/wiremockrefresh",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/configuration",
        "//src/integration/payment:rpc",
        "//src/integration/transfer:enums",
        "//src/integration/transfer:rpc",
        "//third_party:com_google_protobuf_protobuf_java",
        "@com_tink_api_grpc//event_producer_service:event_producer_service_java_proto",
        "@com_tink_api_grpc//event_producer_service/events:events_java_proto",
        "@tink_backend//src/libraries/account_identifier",
        "@tink_backend//src/libraries/protobuf_type_utils",
    ],
)
