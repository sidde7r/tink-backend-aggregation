load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "bpm",
    srcs = glob(["**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/cbiglobe",
        "//src/integration/credentials:service",
        "//src/integration/lib",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/agentcapabilities",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/client",
        "//src/integration/payment:enums",
        "//src/libraries/agent-storage",
        "//third_party:com_google_inject_guice",
        "//third_party:lombok_libs",
        "//third_party:org_slf4j_slf4j_api",
    ],
)
