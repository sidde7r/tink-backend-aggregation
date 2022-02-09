load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "contexts",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/credentials/src/main/java/se/tink/libraries/credentials/service",
        "//src/integration/identity_data",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/api",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/eidasidentity/identity",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/logmasker",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/controllers/configuration/iface",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/log/executor",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/raw_data_events",
        "//src/integration/models",
        "//src/integration/transfer:rpc",
        "//src/libraries/agent-storage",
        "//src/libraries/i18n_aggregation",
        "//src/libraries/metrics:registry",
        "//src/tink_backend_libs:har_logger",
        "//third_party:com_google_code_findbugs_jsr305",
        "@tink_backend//src/aggregation-controller/api/src/main/java/se/tink/backend/aggregationcontroller/v1/rpc/enums",
        "@tink_backend//src/libraries/connectivity_errors/proto:java_proto",
        "@tink_backend//src/libraries/unleash",
    ],
)
