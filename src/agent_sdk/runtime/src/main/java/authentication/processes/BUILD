load("//tools/bzl:lib_with_sources.bzl", "java_library_with_sources")

java_library_with_sources(
    name = "processes",
    srcs = glob(["*.java"]),
    extdeps = [
        "@aggregation//:com_google_guava_guava",
    ],
    intdeps = [
        "//src/agent_sdk/runtime/src/main/java/instance",
        "//src/agent_sdk/sdk",
    ],
    visibility = ["//src/agent_sdk/runtime:__subpackages__"],
)
