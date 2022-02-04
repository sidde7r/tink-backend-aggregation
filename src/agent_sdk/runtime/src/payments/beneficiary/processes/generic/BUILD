load("//tools/bzl:lib_with_sources.bzl", "java_library_with_sources")

java_library_with_sources(
    name = "generic",
    srcs = glob(["*.java"]),
    intdeps = [
        "//src/agent_sdk/runtime/src/payments/beneficiary/process",
        "//src/agent_sdk/runtime/src/instance",
        "//src/agent_sdk/sdk/src/models/payments",
        "//src/agent_sdk/sdk/src/payments/beneficiary",
        "//src/agent_sdk/sdk/src/payments/features/beneficiary",
    ],
    visibility = [
        "//src/agent_sdk/runtime:__subpackages__",
    ],
)
