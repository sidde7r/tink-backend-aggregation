java_library(
    name = "agentsservice",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/annotations",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/configuration/eidas/proxy",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/configuration/integrations",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/configuration/integrations/abnamro",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/configuration/signaturekeypair",
        "//src/integration/tpp_secrets_service/client/src/main/java/se/tink/backend/integration/tpp_secrets_service/client/configuration",
        "//third_party:com_fasterxml_jackson_core_jackson_annotations",
        "//third_party:com_google_guava_guava",
        "//third_party:lombok_libs",
        "@org_slf4j_slf4j_api",
        "@tink_backend//src/agent-data-availability-tracker/src/main/java/se/tink/backend/agent_data_availability_tracker/client",
    ],
)
