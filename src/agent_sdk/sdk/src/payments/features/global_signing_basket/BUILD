load("//tools/bzl:lib_with_sources.bzl", "java_library_with_sources")

java_library_with_sources(
    name = "global_signing_basket",
    srcs = glob(["*.java"]),
    intdeps = [
        "//src/agent_sdk/sdk/src/payments/global_signing_basket",
    ],
    visibility = [
        "//src/agent_sdk:__subpackages__",
    ],
)
