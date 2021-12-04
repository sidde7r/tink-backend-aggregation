load("//tools/bzl:lib_with_sources.bzl", "java_library_with_sources")

java_library_with_sources(
    name = "environment",
    srcs = glob(["**/*.java"]),
    extdeps = [
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/client",
    ],
    intdeps = [
        "//src/agent_sdk/sdk/src/operation",
        "//src/agent_sdk/sdk/src/utils",
        "//src/agent_sdk/sdk/src/storage",
        "//src/agent_sdk/sdk/src/operation/aggregator_configuration",
    ],
    visibility = ["//visibility:public"],
    exports = [
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/client",
    ],
)
