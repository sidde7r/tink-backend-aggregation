load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "cajasur",
    srcs = glob(["**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/credentials:service",
        "//src/integration/lib",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/agentcapabilities",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/client",
        "//src/libraries/agent-storage",
        "//third_party:com_google_inject_guice",
        "//third_party:lombok_libs",
        "//third_party:org_mozilla_rhino",
        "@aggregation//:com_google_guava_guava",
    ],
)
