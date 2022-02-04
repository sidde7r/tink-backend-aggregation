load("//tools/bzl:lib_with_sources.bzl", "java_library_with_sources")

java_library_with_sources(
    name = "generic",
    srcs = glob(["*.java"]),
    intdeps = [
        "//src/agent_sdk/runtime/src/payments/bulk/process",
        "//src/agent_sdk/runtime/src/instance",
        "//src/agent_sdk/sdk/src/payments/bulk",
        "//src/agent_sdk/sdk/src/payments/features/bulk",
    ],
    visibility = [
        "//src/agent_sdk/runtime:__subpackages__",
    ],
)
