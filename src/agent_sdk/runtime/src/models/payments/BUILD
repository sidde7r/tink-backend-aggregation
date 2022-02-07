load("//tools/bzl:lib_with_sources.bzl", "java_library_with_sources")

java_library_with_sources(
    name = "payments",
    srcs = glob(["*.java"]),
    extdeps = [
        "@aggregation//:org_projectlombok_lombok",
        "@aggregation//:com_google_code_findbugs_jsr305",
    ],
    intdeps = [
        "//src/agent_sdk/sdk/src/models/payments",
        "//src/agent_sdk/sdk/src/storage",
    ],
    plugins = [
        "//src/agent_sdk:lombok_plugin",
    ],
    visibility = [
        "//src/agent_sdk/compatibility_layers:__subpackages__",
        "//src/agent_sdk/runtime:__subpackages__",
    ],
    exports = [
        "//src/agent_sdk/sdk/src/models/payments",
    ],
)
