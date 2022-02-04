load("//tools/bzl:lib_with_sources.bzl", "java_library_with_sources")

java_library_with_sources(
    name = "runtime",
    intdeps = [
        "//src/agent_sdk/runtime/src/instance",
        "//src/agent_sdk/runtime/src/payments",
        "//src/agent_sdk/runtime/src/authentication",
    ],
    visibility = [
        "//src/agent_sdk/compatibility_layers:__subpackages__",
        "//src/agent_sdk/runtime:__subpackages__",
    ],
    exports = [
        "//src/agent_sdk/runtime/src/instance",
        "//src/agent_sdk/runtime/src/payments",
    ],
)
