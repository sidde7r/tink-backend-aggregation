load("//tools/bzl:lib_with_sources.bzl", "java_library_with_sources")

java_library_with_sources(
    name = "generic",
    srcs = glob(["*.java"]),
    intdeps = [
        "//src/agent_sdk/sdk/src/authentication/consent",
        "//src/agent_sdk/sdk/src/steppable_execution/execution_flow",
        "//src/agent_sdk/sdk/src/steppable_execution/interactive_step",
        "//src/agent_sdk/sdk/src/steppable_execution/non_interactive_step",
    ],
    visibility = ["//src/agent_sdk/sdk:__subpackages__"],
)
