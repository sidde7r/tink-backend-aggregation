load("//tools/bzl:lib_with_sources.bzl", "java_library_with_sources")

java_library_with_sources(
    name = "generic",
    srcs = glob(["*.java"]),
    intdeps = [
        "//src/agent_sdk/sdk/src/authentication/consent",
        "//src/agent_sdk/sdk/src/authentication/steppable_execution",
    ],
    visibility = ["//src/agent_sdk/sdk:__subpackages__"],
)
