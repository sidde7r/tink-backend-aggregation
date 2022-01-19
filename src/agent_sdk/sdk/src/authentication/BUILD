load("//tools/bzl:lib_with_sources.bzl", "java_library_with_sources")

java_library_with_sources(
    name = "authentication",
    intdeps = [
        "//src/agent_sdk/sdk/src/authentication/authenticators",
        "//src/agent_sdk/sdk/src/authentication/base_steps",
        "//src/agent_sdk/sdk/src/authentication/common_steps",
        "//src/agent_sdk/sdk/src/authentication/consent",
        "//src/agent_sdk/sdk/src/authentication/features",
    ],
    visibility = ["//src/agent_sdk/sdk:__subpackages__"],
    exports = [
        "//src/agent_sdk/sdk/src/authentication/authenticators",
        "//src/agent_sdk/sdk/src/authentication/base_steps",
        "//src/agent_sdk/sdk/src/authentication/common_steps",
        "//src/agent_sdk/sdk/src/authentication/consent",
        "//src/agent_sdk/sdk/src/authentication/features",
    ],
)
