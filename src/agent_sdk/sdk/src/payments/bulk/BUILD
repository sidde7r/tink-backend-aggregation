load("//tools/bzl:lib_with_sources.bzl", "java_library_with_sources")

java_library_with_sources(
    name = "bulk",
    intdeps = [
        "//src/agent_sdk/sdk/src/payments/bulk/generic",
        "//src/agent_sdk/sdk/src/payments/bulk/steppable_execution",
    ],
    visibility = [
        "//src/agent_sdk:__subpackages__",
    ],
    exports = [
        "//src/agent_sdk/sdk/src/payments/bulk/generic",
        "//src/agent_sdk/sdk/src/payments/bulk/steppable_execution",
    ],
)
