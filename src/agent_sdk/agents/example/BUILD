load("//tools/bzl:lib_with_sources.bzl", "java_library_with_sources")

java_library_with_sources(
    name = "example",
    srcs = glob(["src/**/*.java"]),
    intdeps = [
        "//src/agent_sdk/sdk",
    ],
    visibility = [
        "//visibility:public",
    ],
)
