load("//tools/bzl:lib_with_sources.bzl", "java_library_with_sources")

java_library_with_sources(
    name = "operation",
    srcs = glob(["**/*.java"]),
    extdeps = [
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/controllers/configuration/iface",
    ],
    intdeps = [
        "//src/agent_sdk/sdk/src/models/field",
        "//src/agent_sdk/sdk/src/storage",
        "//src/agent_sdk/sdk/src/utils",
    ],
    visibility = ["//visibility:public"],
)
