load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "eidasidentity",
    srcs = glob(["*.java"]),
    data = [
    ],
    visibility = ["//visibility:public"],
    deps = [
        "//src/aggregation/lib/src/main/java/se/tink/backend/aggregation/workers/context",
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/credentials/src/main/java/se/tink/libraries/credentials/service",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/configuration/agents/utils",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/eidasidentity/identity",
        "//src/integration/tpp_secrets_service/client/src/main/java/se/tink/backend/integration/tpp_secrets_service/client:tpp-secrets-service-client",
        "//src/integration/tpp_secrets_service/client/src/main/java/se/tink/backend/integration/tpp_secrets_service/client/entities",
        "//src/integration/tpp_secrets_service/client/src/main/java/se/tink/backend/integration/tpp_secrets_service/client/iface",
        "//third_party:com_google_inject_guice",
        "//third_party:lombok_libs",
        "//third_party:org_slf4j_slf4j_api",
        "@maven//:no_finn_unleash_unleash_client_java",
        "@tink_backend//src/libraries/unleash",
    ],
)
