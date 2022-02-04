load("//tools/bzl:lib_with_sources.bzl", "java_library_with_sources")

java_library_with_sources(
    name = "bulk",
    srcs = glob(["*.java"]),
    intdeps = [
        "//src/agent_sdk/sdk/src/payments/bulk",
    ],
    visibility = [
        "//src/agent_sdk:__subpackages__",
    ],
)
