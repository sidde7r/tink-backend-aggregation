java_library(
    name = "postepay",
    srcs = glob(["**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/aggregation/lib/src/main/java/se/tink/backend/aggregation/client/provider_configuration/rpc",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/it/openbanking/bancoposta",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/cbiglobe",
        "//src/integration/credentials:service",
        "//src/integration/lib",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/agentcapabilities",
        "//src/libraries/serialization",
        "//third_party:com_google_inject_guice",
        "//third_party:lombok_libs",
    ],
)
