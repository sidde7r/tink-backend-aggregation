load("//tools/bzl:lib_with_sources.bzl", "java_library_with_sources")

java_library_with_sources(
    name = "pagination_result",
    srcs = glob(["*.java"]),
    extdeps = [
        "@aggregation//:org_projectlombok_lombok",
        "//src/agent_sdk/sdk/src/models/transaction",
    ],
    plugins = [
        "//src/agent_sdk:lombok_plugin",
    ],
    visibility = ["//src/agent_sdk:__subpackages__"],
)
