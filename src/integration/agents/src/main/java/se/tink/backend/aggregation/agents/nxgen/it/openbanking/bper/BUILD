java_library(
    name = "bper",
    srcs = glob(["**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/aggregation/lib/src/main/java/se/tink/backend/aggregation/client/provider_configuration/rpc",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/cbiglobe",
        "//src/integration/credentials:service",
        "//src/integration/lib",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/agentcapabilities",
        "//src/integration/transfer:enums",
        "//src/integration/transfer:rpc",
        "//third_party:com_google_inject_guice",
        "@tink_backend//src/libraries/payments_common/src/java/se/tink/libraries/payments/common/model:payments_common_model",
    ],
)
