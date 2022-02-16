load("//tools/bzl:lib_with_sources.bzl", "java_library_with_sources")

java_library_with_sources(
    name = "index",
    srcs = glob(["*.java"]),
    extdeps = [
        "@aggregation//:org_projectlombok_lombok",
    ],
    intdeps = [
        "//src/agent_sdk/sdk/src/fetching/transactions/pagination_result",
        "//src/agent_sdk/sdk/src/fetching/transactions:fetcher",
        "//src/agent_sdk/sdk/src/storage",
    ],
    plugins = [
        "//src/agent_sdk:lombok_plugin",
    ],
    visibility = ["//src/agent_sdk:__subpackages__"],
)
