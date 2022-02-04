load("//tools/bzl:lib_with_sources.bzl", "java_library_with_sources")

java_library_with_sources(
    name = "modules",
    srcs = glob(["*.java"]),
    extdeps = [
        "@aggregation//:com_google_inject_guice",
        "@aggregation//:org_slf4j_slf4j_api",
        "@aggregation//:org_projectlombok_lombok",
        "//src/integration/credentials/src/main/java/se/tink/libraries/credentials/service",
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/user:rpc",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/contexts",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/agents/componentproviders",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/controllers/configuration/iface",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/configuration/agentsservice",
    ],
    intdeps = [
        "//src/agent_sdk/sdk/src/environment",
        "//src/agent_sdk/sdk/src/operation",
        "//src/agent_sdk/sdk/src/storage",
        "//src/agent_sdk/sdk/src/utils",
        "//src/agent_sdk/runtime/src/environment",
        "//src/agent_sdk/runtime/src/operation",
        "//src/agent_sdk/runtime/src/operation/aggregator_configuration",
        "//src/agent_sdk/runtime/src/storage",
        "//src/agent_sdk/runtime/src/utils",
    ],
    plugins = [
        "//src/agent_sdk:lombok_plugin",
    ],
    visibility = ["//visibility:public"],
)
