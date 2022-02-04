load("//tools/bzl:lib_with_sources.bzl", "java_library_with_sources")

java_library_with_sources(
    name = "bulk",
    srcs = glob(["*.java"]),
    intdeps = [
        "//src/agent_sdk/runtime/src/payments/bulk/process",
        "//src/agent_sdk/runtime/src/payments/bulk/processes",
        "//src/agent_sdk/sdk/src/payments/bulk",
        "//src/agent_sdk/sdk/src/models/payments",
    ],
    visibility = [
        "//src/agent_sdk/compatibility_layers:__subpackages__",
        "//src/agent_sdk/runtime:__subpackages__",
    ],
    exports = [
        "//src/agent_sdk/runtime/src/payments/bulk/processes",
    ],
)
