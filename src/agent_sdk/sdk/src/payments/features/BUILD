load("//tools/bzl:lib_with_sources.bzl", "java_library_with_sources")

java_library_with_sources(
    name = "features",
    intdeps = [
        "//src/agent_sdk/sdk/src/payments/features/beneficiary",
        "//src/agent_sdk/sdk/src/payments/features/bulk",
        "//src/agent_sdk/sdk/src/payments/features/global_signing_basket",
        "//src/agent_sdk/sdk/src/payments/features/single",
    ],
    visibility = [
        "//src/agent_sdk:__subpackages__",
    ],
    exports = [
        "//src/agent_sdk/sdk/src/payments/features/beneficiary",
        "//src/agent_sdk/sdk/src/payments/features/bulk",
        "//src/agent_sdk/sdk/src/payments/features/global_signing_basket",
        "//src/agent_sdk/sdk/src/payments/features/single",
    ],
)
