load("//tools/bzl:lib_with_sources.bzl", "java_library_with_sources")

java_library_with_sources(
    name = "aggregator_configuration",
    srcs = glob(["*.java"]),
    intdeps = [
        "//src/agent_sdk/sdk/src/operation/aggregator_configuration",
        "//src/agent_sdk/sdk/src/utils/serialization",
    ],
    visibility = [
        "//src/agent_sdk/compatibility_layers:__subpackages__",
        "//src/agent_sdk/runtime:__subpackages__",
    ],
)
