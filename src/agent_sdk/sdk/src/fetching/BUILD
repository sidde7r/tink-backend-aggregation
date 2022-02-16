load("//tools/bzl:lib_with_sources.bzl", "java_library_with_sources")

java_library_with_sources(
    name = "fetching",
    intdeps = [
        "//src/agent_sdk/sdk/src/fetching/accounts",
        "//src/agent_sdk/sdk/src/fetching/features",
        "//src/agent_sdk/sdk/src/fetching/identity_data",
        "//src/agent_sdk/sdk/src/fetching/transactions",
    ],
    visibility = ["//src/agent_sdk:__subpackages__"],
    exports = [
        "//src/agent_sdk/sdk/src/fetching/accounts",
        "//src/agent_sdk/sdk/src/fetching/features",
        "//src/agent_sdk/sdk/src/fetching/identity_data",
        "//src/agent_sdk/sdk/src/fetching/transactions",
    ],
)
