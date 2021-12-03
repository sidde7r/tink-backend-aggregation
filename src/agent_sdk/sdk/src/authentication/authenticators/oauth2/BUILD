load("//tools/bzl:lib_with_sources.bzl", "java_library_with_sources")

java_library_with_sources(
    name = "oauth2",
    srcs = glob(["**/*.java"]),
    extdeps = [
        "@agent_sdk_maven//:com_google_guava_guava",
        "@agent_sdk_maven//:org_projectlombok_lombok",
        "@agent_sdk_maven//:org_slf4j_slf4j_api",
        "//src/agent_sdk:lombok_plugin",
    ],
    intdeps = [
        "@tink_backend//src/libraries/serialization_utils",
        "//src/agent_sdk/sdk/src/authentication/new_consent",
        "//src/agent_sdk/sdk/src/authentication/existing_consent",
        "//src/agent_sdk/sdk/src/authentication/common_steps",
        "//src/agent_sdk/sdk/src/models/authentication",
        "//src/agent_sdk/sdk/src/operation",
        "//src/agent_sdk/sdk/src/storage",
        "//src/agent_sdk/sdk/src/user_interaction",
        "//src/agent_sdk/sdk/src/exceptions",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/url",
    ],
    visibility = ["//visibility:public"],
    exports = [
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/url",
    ],
)
