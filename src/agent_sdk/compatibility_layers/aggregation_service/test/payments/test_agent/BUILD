load("//tools/bzl:lib_with_sources.bzl", "java_library_with_sources")

java_library_with_sources(
    name = "test_agent",
    srcs = glob(["*.java"]),
    extdeps = [
        "@aggregation//:org_projectlombok_lombok",
        "@aggregation//:org_apache_commons_commons_lang3",
        "@aggregation//:junit_junit",
    ],
    intdeps = [
        "//src/agent_sdk/sdk/src/payments",
        "//src/agent_sdk/sdk/src/models/payments",
        "//src/agent_sdk/sdk/src/steppable_execution",
    ],
    plugins = [
        "//src/agent_sdk:lombok_plugin",
    ],
    visibility = ["//visibility:public"],
)
