load("//tools/bzl:lib_with_sources.bzl", "java_library_with_sources")

java_library_with_sources(
    name = "generic",
    srcs = glob(["*.java"]),
    extdeps = [
        "@aggregation//:com_google_guava_guava",
    ],
    intdeps = [
        "//src/agent_sdk/sdk/src/models/payments",
        "//src/agent_sdk/sdk/src/payments/single/steppable_execution",
    ],
    visibility = [
        "//src/agent_sdk:__subpackages__",
    ],
)
