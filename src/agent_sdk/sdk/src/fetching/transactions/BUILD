load("//tools/bzl:lib_with_sources.bzl", "java_library_with_sources")

java_library_with_sources(
    name = "fetcher",
    srcs = ["TransactionsFetcher.java"],
    visibility = ["//src/agent_sdk/sdk/src/fetching:__subpackages__"],
)

java_library_with_sources(
    name = "transactions",
    intdeps = [
        ":fetcher",
        "//src/agent_sdk/sdk/src/fetching/transactions/pagination_result",
        "//src/agent_sdk/sdk/src/fetching/transactions/paginators/date",
        "//src/agent_sdk/sdk/src/fetching/transactions/paginators/index",
        "//src/agent_sdk/sdk/src/fetching/transactions/paginators/key",
    ],
    visibility = ["//src/agent_sdk:__subpackages__"],
    exports = [
        ":fetcher",
        "//src/agent_sdk/sdk/src/fetching/transactions/pagination_result",
        "//src/agent_sdk/sdk/src/fetching/transactions/paginators/date",
        "//src/agent_sdk/sdk/src/fetching/transactions/paginators/index",
        "//src/agent_sdk/sdk/src/fetching/transactions/paginators/key",
    ],
)
