load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "dk",
    srcs = glob(["**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/credentials:service",
        "//src/integration/lib",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/agentcapabilities",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/contexts/agent",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/utils/supplementalfields",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/controllers/authentication/multifactor/nemid",
        "//third_party:com_google_inject_guice",
        "//third_party:lombok_libs",
        "//third_party:org_apache_commons_commons_lang3",
        "//third_party:org_slf4j_slf4j_api",
        "@aggregation//:com_google_guava_guava",
        "@tink_backend//src/libraries/account_identifier",
    ],
)
