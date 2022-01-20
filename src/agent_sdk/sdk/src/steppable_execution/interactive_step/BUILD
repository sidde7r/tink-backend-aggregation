load("//tools/bzl:lib_with_sources.bzl", "java_library_with_sources")

java_library_with_sources(
    name = "interactive_step",
    srcs = glob(["**/*.java"]),
    extdeps = [
        "@aggregation//:com_google_guava_guava",
        "@aggregation//:com_google_code_findbugs_jsr305",
    ],
    intdeps = [
        "//src/agent_sdk/sdk/src/steppable_execution/base_step",
        "//src/agent_sdk/sdk/src/user_interaction",
    ],
    visibility = ["//src/agent_sdk/sdk:__subpackages__"],
)
