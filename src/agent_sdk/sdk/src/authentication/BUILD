load("//tools/bzl:lib_with_sources.bzl", "java_library_with_sources")

java_library_with_sources(
    name = "authentication",
    intdeps = [
        "//src/agent_sdk/sdk/src/authentication/authenticators",
        "//src/agent_sdk/sdk/src/authentication/capability",
        "//src/agent_sdk/sdk/src/authentication/common_steps",
        "//src/agent_sdk/sdk/src/authentication/existing_consent",
        "//src/agent_sdk/sdk/src/authentication/new_consent",
    ],
    visibility = ["//visibility:public"],
    exports = [
        "//src/agent_sdk/sdk/src/authentication/authenticators",
        "//src/agent_sdk/sdk/src/authentication/capability",
        "//src/agent_sdk/sdk/src/authentication/common_steps",
        "//src/agent_sdk/sdk/src/authentication/existing_consent",
        "//src/agent_sdk/sdk/src/authentication/new_consent",
    ],
)
