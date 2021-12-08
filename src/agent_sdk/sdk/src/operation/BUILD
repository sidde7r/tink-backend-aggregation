load("//tools/bzl:lib_with_sources.bzl", "java_library_with_sources")

alias(
    name = "eidas_proxy_configuration",
    actual = "//src/integration/lib/src/main/java/se/tink/backend/aggregation/configuration/eidas/proxy",
)

java_library_with_sources(
    name = "operation",
    srcs = glob(["**/*.java"]),
    extdeps = [
        "@agent_sdk_maven//:com_google_guava_guava",
        ":eidas_proxy_configuration",
        "@agent_sdk_maven//:com_google_code_findbugs_jsr305",
    ],
    intdeps = [
        "//src/agent_sdk/sdk/src/models/field",
        "//src/agent_sdk/sdk/src/storage",
        "//src/agent_sdk/sdk/src/utils",
        "//src/agent_sdk/sdk/src/user_interaction",
        "//src/agent_sdk/sdk/src/operation/aggregator_configuration",
    ],
    visibility = ["//visibility:public"],
    exports = [
        ":eidas_proxy_configuration",
        "//src/agent_sdk/sdk/src/operation/aggregator_configuration",
    ],
)
