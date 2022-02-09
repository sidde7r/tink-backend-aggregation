load("//tools/bzl:lib_with_sources.bzl", "java_library_with_sources")

java_library_with_sources(
    name = "steppable_execution",
    srcs = glob(["*.java"]),
    extdeps = [
        "@aggregation//:com_google_code_findbugs_jsr305",
        "@aggregation//:com_google_guava_guava",
    ],
    intdeps = [
        "//src/agent_sdk/sdk/src/steppable_execution",
    ],
    visibility = [
        "//src/agent_sdk/compatibility_layers:__subpackages__",
        "//src/agent_sdk/runtime:__subpackages__",
    ],
)
