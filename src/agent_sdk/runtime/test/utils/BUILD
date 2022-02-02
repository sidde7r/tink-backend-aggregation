load("//tools/bzl:lib_with_sources.bzl", "java_library_with_sources")

java_library_with_sources(
    name = "utils",
    srcs = glob(["*.java"]),
    extdeps = [
        "@aggregation//:commons_codec_commons_codec",
    ],
    intdeps = [
        "//src/agent_sdk/sdk/src/utils",
    ],
    visibility = [
        "//src/agent_sdk/compatibility_layers:__subpackages__",
        "//src/agent_sdk/runtime:__subpackages__",
    ],
)
