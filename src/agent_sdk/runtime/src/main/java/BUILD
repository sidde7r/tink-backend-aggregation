load("//tools/bzl:lib_with_sources.bzl", "java_library_with_sources")

java_library_with_sources(
    name = "agent_runtime",
    intdeps = [
        "//src/agent_sdk/runtime/src/main/java/instance",
        "//src/agent_sdk/runtime/src/main/java/authentication",
        "//src/agent_sdk/sdk",
    ],
    visibility = ["//visibility:public"],
)
