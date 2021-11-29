load("//tools/bzl:lib_with_sources.bzl", "java_library_with_sources")

java_library_with_sources(
    name = "sdk",
    srcs = glob(["src/**/*.java"]),
    extdeps = [
        "@agent_sdk_maven//:com_fasterxml_jackson_core_jackson_core",
        "@agent_sdk_maven//:com_google_code_findbugs_jsr305",
        "@agent_sdk_maven//:com_google_guava_guava",
        "@agent_sdk_maven//:org_projectlombok_lombok",
        "@agent_sdk_maven//:org_slf4j_slf4j_api",
        "//src/agent_sdk:lombok_plugin",
    ],
    intdeps = [
        "@tink_backend//src/libraries/serialization_utils",
        "//src/agent_sdk/sdk/src/models/authentication",
        "//src/agent_sdk/sdk/src/user_interaction",
        "//src/agent_sdk/sdk/src/environment",
        "//src/agent_sdk/sdk/src/storage",
        "@tink_backend//src/libraries/account_identifier",
    ],
    visibility = ["//visibility:public"],
    exports = [
        "//src/agent_sdk:lombok_plugin",
        "//src/agent_sdk/sdk/src/environment",
        "//src/agent_sdk/sdk/src/models/authentication",
        "//src/agent_sdk/sdk/src/storage",
        "//src/agent_sdk/sdk/src/user_interaction",
        "@agent_sdk_maven//:com_google_guava_guava",
        "@tink_backend//src/libraries/account_identifier",
    ],
)
