java_library(
    name = "handelsbanken",
    srcs = glob(["**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/aggregation/lib/src/main/java/se/tink/backend/aggregation/client/provider_configuration/rpc",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/handelsbanken",
        "//src/integration/credentials:service",
        "//src/integration/lib",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/agentcapabilities",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/contexts/agent",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/configuration/signaturekeypair",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/eidasidentity/identity",
        "//third_party:com_google_http_client_google_http_client",
        "//third_party:com_google_inject_guice",
        "//third_party:org_slf4j_slf4j_api",
        "@aggregation//:com_google_guava_guava",
    ],
)
