load("//tools/bzl:lib_with_sources.bzl", "java_library_with_sources")

java_library_with_sources(
    name = "steppable_execution",
    srcs = glob(["*.java"]),
    extdeps = [
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/controllers/utils",
    ],
    intdeps = [
        "//src/agent_sdk/runtime/src/steppable_execution",
        "//src/agent_sdk/sdk/src/steppable_execution",
        "//src/agent_sdk/sdk/src/storage",
        "//src/agent_sdk/sdk/src/user_interaction",
        "//src/agent_sdk/runtime/src/user_interaction",
    ],
    visibility = [
        "//src/agent_sdk/compatibility_layers:__subpackages__",
    ],
)
