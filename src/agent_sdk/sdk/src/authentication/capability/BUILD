load("//tools/bzl:lib_with_sources.bzl", "java_library_with_sources")

java_library_with_sources(
    name = "capability",
    srcs = glob(["*.java"]),
    intdeps = [
        "//src/agent_sdk/sdk/src/authentication/authenticators",
    ],
    visibility = ["//visibility:public"],
)
