load("//tools/bzl:lib_with_sources.bzl", "java_library_with_sources")

java_library_with_sources(
    name = "payments",
    intdeps = [
        "//src/agent_sdk/sdk/src/payments/features",
        "//src/agent_sdk/sdk/src/payments/beneficiary",
        "//src/agent_sdk/sdk/src/payments/bulk",
        "//src/agent_sdk/sdk/src/payments/global_signing_basket",
        "//src/agent_sdk/sdk/src/payments/single",
    ],
    visibility = ["//src/agent_sdk:__subpackages__"],
    exports = [
        "//src/agent_sdk/sdk/src/payments/beneficiary",
        "//src/agent_sdk/sdk/src/payments/bulk",
        "//src/agent_sdk/sdk/src/payments/features",
        "//src/agent_sdk/sdk/src/payments/global_signing_basket",
        "//src/agent_sdk/sdk/src/payments/single",
    ],
)
