load("//tools/bzl:lib_with_sources.bzl", "java_library_with_sources")

java_library_with_sources(
    name = "berlingroup",
    srcs = glob(["**/*.java"]),
    extdeps = [
        "//src/agent_sdk:lombok_libs",
    ],
    intdeps = [
        "//src/agent_sdk/sdk/src/authentication/new_consent",
        "//src/agent_sdk/sdk/src/authentication/existing_consent",
        "//src/agent_sdk/sdk/src/operation",
        "//src/agent_sdk/sdk/src/storage",
        "//src/agent_sdk/sdk/src/utils",
        "//src/agent_sdk/sdk/src/user_interaction",
        "//src/agent_sdk/sdk/src/exceptions",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/url",
    ],
    visibility = ["//src/agent_sdk/sdk:__subpackages__"],
    exports = [
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/url",
    ],
)
