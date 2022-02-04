load("//tools/bzl:lib_with_sources.bzl", "java_library_with_sources")

java_library_with_sources(
    name = "global_signing_basket",
    srcs = glob(["*.java"]),
    intdeps = [
        "//src/agent_sdk/sdk/src/payments/global_signing_basket",
        "//src/agent_sdk/sdk/src/models/payments",
    ],
    visibility = [
        "//src/agent_sdk/compatibility_layers:__subpackages__",
        "//src/agent_sdk/runtime:__subpackages__",
    ],
    exports = [
        "//src/agent_sdk/sdk/src/payments/features/global_signing_basket",
    ],
)
