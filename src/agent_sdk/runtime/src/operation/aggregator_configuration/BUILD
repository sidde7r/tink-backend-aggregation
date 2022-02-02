load("//tools/bzl:lib_with_sources.bzl", "java_library_with_sources")

java_library_with_sources(
    name = "aggregator_configuration",
    srcs = glob(["*.java"]),
    extdeps = ["@aggregation//:com_fasterxml_jackson_core_jackson_databind"],
    intdeps = [
        "//src/agent_sdk/sdk/src/operation/aggregator_configuration",
    ],
    visibility = [
        "//src/agent_sdk/compatibility_layers:__subpackages__",
        "//src/agent_sdk/runtime:__subpackages__",
    ],
)
