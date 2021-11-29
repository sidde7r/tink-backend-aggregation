load("//tools/bzl:lib_with_sources.bzl", "java_library_with_sources")

java_library_with_sources(
    name = "environment",
    srcs = glob(["*.java"]),
    extdeps = [
        "@agent_sdk_maven//:com_google_guava_guava",
    ],
    intdeps = [
        "//src/agent_sdk/sdk/src/user_interaction",
    ],
    visibility = ["//visibility:public"],
)
