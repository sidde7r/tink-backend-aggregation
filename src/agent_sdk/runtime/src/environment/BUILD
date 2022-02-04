load("//tools/bzl:lib_with_sources.bzl", "java_library_with_sources")

java_library_with_sources(
    name = "environment",
    srcs = glob(["*.java"]),
    extdeps = [
        "@aggregation//:org_projectlombok_lombok",
    ],
    intdeps = [
        "//src/agent_sdk/sdk/src/environment",
        "//src/agent_sdk/sdk/src/utils",
        "//src/agent_sdk/sdk/src/operation/aggregator_configuration",
        "//src/agent_sdk/sdk/src/operation/http",
        "//src/agent_sdk/sdk/src/operation",
        "//src/agent_sdk/sdk/src/storage",
    ],
    plugins = [
        "//src/agent_sdk:lombok_plugin",
    ],
    visibility = [
        "//src/agent_sdk/compatibility_layers:__subpackages__",
        "//src/agent_sdk/runtime:__subpackages__",
    ],
)
