load("//tools/bzl:lib_with_sources.bzl", "java_library_with_sources")

java_library_with_sources(
    name = "process",
    srcs = glob(["*.java"]),
    intdeps = [
        "//src/agent_sdk/runtime/src/instance",
        "//src/agent_sdk/sdk/src/payments/beneficiary/generic",
    ],
    visibility = [
        "//src/agent_sdk/runtime:__subpackages__",
    ],
)
