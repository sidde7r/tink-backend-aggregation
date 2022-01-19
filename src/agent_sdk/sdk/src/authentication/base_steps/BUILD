load("//tools/bzl:lib_with_sources.bzl", "java_library_with_sources")

java_library_with_sources(
    name = "base_steps",
    srcs = glob(["*.java"]),
    intdeps = [
        "//src/agent_sdk/sdk/src/steppable_execution:steps",
        "//src/agent_sdk/sdk/src/authentication/consent",
    ],
    visibility = ["//src/agent_sdk/sdk:__subpackages__"],
)
