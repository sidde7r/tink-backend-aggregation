load("//tools/bzl:lib_with_sources.bzl", "java_library_with_sources")

java_library_with_sources(
    name = "storage",
    srcs = glob(["*.java"]),
    extdeps = [
        "@aggregation//:com_fasterxml_jackson_core_jackson_core",
        "@aggregation//:org_projectlombok_lombok",
    ],
    intdeps = [
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc:field",
        "//src/libraries/serialization",
    ],
    plugins = [
        "//src/agent_sdk:lombok_plugin",
    ],
    visibility = [
        "//src/agent_sdk/compatibility_layers:__subpackages__",
        "//src/agent_sdk/runtime:__subpackages__",
    ],
)
