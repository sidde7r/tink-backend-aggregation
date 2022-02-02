load("//tools/bzl:lib_with_sources.bzl", "java_library_with_sources")

java_library_with_sources(
    name = "steps",
    intdeps = [
        "//src/agent_sdk/sdk/src/steppable_execution/base_step",
        "//src/agent_sdk/sdk/src/steppable_execution/interactive_step",
        "//src/agent_sdk/sdk/src/steppable_execution/non_interactive_step",
    ],
    visibility = ["//src/agent_sdk:__subpackages__"],
    exports = [
        "//src/agent_sdk/sdk/src/steppable_execution/base_step",
        "//src/agent_sdk/sdk/src/steppable_execution/interactive_step",
        "//src/agent_sdk/sdk/src/steppable_execution/non_interactive_step",
    ],
)

java_library_with_sources(
    name = "steppable_execution",
    intdeps = [
        ":steps",
        "//src/agent_sdk/sdk/src/steppable_execution/execution_flow",
    ],
    visibility = ["//src/agent_sdk:__subpackages__"],
    exports = [
        ":steps",
        "//src/agent_sdk/sdk/src/steppable_execution/execution_flow",
    ],
)
