load("//tools/bzl:lib_with_sources.bzl", "java_library_with_sources")

java_library_with_sources(
    name = "beneficiary",
    srcs = glob(["*.java"]),
    intdeps = [
        "//src/agent_sdk/sdk/src/models/payments",
        "//src/agent_sdk/sdk/src/payments/features/beneficiary",
        "//src/agent_sdk/sdk/src/payments/beneficiary",
        "//src/agent_sdk/runtime/src/payments/beneficiary/process",
        "//src/agent_sdk/runtime/src/payments/beneficiary/processes",
    ],
    visibility = [
        "//src/agent_sdk/compatibility_layers:__subpackages__",
        "//src/agent_sdk/runtime:__subpackages__",
    ],
    exports = [
        "//src/agent_sdk/runtime/src/payments/beneficiary/processes",
        "//src/agent_sdk/sdk/src/payments/beneficiary",
        "//src/agent_sdk/sdk/src/payments/features/beneficiary",
    ],
)
