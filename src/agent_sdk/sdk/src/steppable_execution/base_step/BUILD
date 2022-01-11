load("//tools/bzl:lib_with_sources.bzl", "java_library_with_sources")

java_library_with_sources(
    name = "base_step",
    srcs = glob(["*.java"]),
    extdeps = [
        "//src/agent_sdk:lombok_libs",
        "@aggregation//:com_google_code_findbugs_jsr305",
    ],
    intdeps = [
        "//src/agent_sdk/sdk/src/storage",
        "//src/agent_sdk/sdk/src/user_interaction",
        "//src/agent_sdk/sdk/src/operation",
    ],
    visibility = ["//src/agent_sdk/sdk:__subpackages__"],
)
