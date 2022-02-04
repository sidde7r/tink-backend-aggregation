load("//tools/bzl:lib_with_sources.bzl", "java_library_with_sources")

java_library_with_sources(
    name = "single",
    intdeps = [
        "//src/agent_sdk/sdk/src/payments/single/generic",
        "//src/agent_sdk/sdk/src/payments/single/steppable_execution",
    ],
    visibility = [
        "//src/agent_sdk:__subpackages__",
    ],
    exports = [
        "//src/agent_sdk/sdk/src/payments/single/generic",
        "//src/agent_sdk/sdk/src/payments/single/steppable_execution",
    ],
)
