load("//tools/bzl:lib_with_sources.bzl", "java_library_with_sources")

java_library_with_sources(
    name = "environment",
    srcs = glob(["*.java"]),
    intdeps = [
        "//src/agent_sdk/sdk",
    ],
    visibility = ["//src/agent_sdk/runtime:__subpackages__"],
)
