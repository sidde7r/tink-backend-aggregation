load("//tools/bzl:lib_with_sources.bzl", "java_library_with_sources")

java_library_with_sources(
    name = "features",
    srcs = glob(["*.java"]),
    intdeps = [
        "//src/agent_sdk/sdk/src/authentication/authenticators",
    ],
    visibility = ["//visibility:public"],
)
