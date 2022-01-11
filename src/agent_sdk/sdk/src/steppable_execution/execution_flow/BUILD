load("//tools/bzl:lib_with_sources.bzl", "java_library_with_sources")

java_library_with_sources(
    name = "execution_flow",
    srcs = glob(["*.java"]),
    extdeps = [
        "@aggregation//:com_google_guava_guava",
        "@aggregation//:com_google_code_findbugs_jsr305",
    ],
    intdeps = [
        "//src/agent_sdk/sdk/src/steppable_execution/base_step",
        "//src/agent_sdk/sdk/src/steppable_execution/interactive_step",
        "//src/agent_sdk/sdk/src/steppable_execution/non_interactive_step",
    ],
    visibility = ["//src/agent_sdk/sdk:__subpackages__"],
)
