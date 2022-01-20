load("//tools/bzl:lib_with_sources.bzl", "java_library_with_sources")

java_library_with_sources(
    name = "common_steps",
    srcs = glob(["*.java"]),
    extdeps = [
        "@aggregation//:com_google_guava_guava",
    ],
    intdeps = [
        "//src/agent_sdk/sdk/src/steppable_execution:steps",
        "//src/agent_sdk/sdk/src/authentication/consent",
        "//src/agent_sdk/sdk/src/authentication/steppable_execution",
        "//src/agent_sdk/sdk/src/user_interaction",
        "//src/agent_sdk/sdk/src/models/field",
    ],
    visibility = ["//src/agent_sdk/sdk:__subpackages__"],
)
