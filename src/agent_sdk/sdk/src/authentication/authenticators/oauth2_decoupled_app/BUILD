load("//tools/bzl:lib_with_sources.bzl", "java_library_with_sources")

java_library_with_sources(
    name = "oauth2_decoupled_app",
    srcs = glob(["**/*.java"]),
    intdeps = [
        "//src/agent_sdk/sdk/src/steppable_execution:steps",
        "//src/agent_sdk/sdk/src/authentication/consent",
        "//src/agent_sdk/sdk/src/authentication/common_steps",
        "//src/agent_sdk/sdk/src/authentication/base_steps",
        "//src/agent_sdk/sdk/src/authentication/authenticators/oauth2",
        "//src/agent_sdk/sdk/src/authentication/authenticators/thirdparty_app",
    ],
    visibility = ["//src/agent_sdk/sdk:__subpackages__"],
)
