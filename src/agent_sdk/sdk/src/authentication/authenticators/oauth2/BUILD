load("//tools/bzl:lib_with_sources.bzl", "java_library_with_sources")

java_library_with_sources(
    name = "oauth2",
    srcs = glob(["**/*.java"]),
    extdeps = [
        "@aggregation//:com_google_guava_guava",
        "@aggregation//:org_slf4j_slf4j_api",
        "//src/agent_sdk:lombok_libs",
    ],
    intdeps = [
        "//src/libraries/serialization",
        "//src/agent_sdk/sdk/src/steppable_execution:steps",
        "//src/agent_sdk/sdk/src/authentication/consent",
        "//src/agent_sdk/sdk/src/authentication/base_steps",
        "//src/agent_sdk/sdk/src/authentication/common_steps",
        "//src/agent_sdk/sdk/src/models/authentication",
        "//src/agent_sdk/sdk/src/operation",
        "//src/agent_sdk/sdk/src/storage",
        "//src/agent_sdk/sdk/src/user_interaction",
        "//src/agent_sdk/sdk/src/exceptions",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/url",
    ],
    visibility = ["//src/agent_sdk/sdk:__subpackages__"],
    exports = [
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/url",
    ],
)
