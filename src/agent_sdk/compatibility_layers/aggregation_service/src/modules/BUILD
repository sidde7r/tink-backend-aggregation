load("//tools/bzl:lib_with_sources.bzl", "java_library_with_sources")

java_library_with_sources(
    name = "modules",
    srcs = glob(["*.java"]),
    extdeps = [
        "@aggregation//:com_google_inject_guice",
        "//src/integration/credentials/src/main/java/se/tink/libraries/credentials/service",
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/user:rpc",
    ],
    intdeps = [
        "//src/agent_sdk/sdk/src/operation",
        "//src/agent_sdk/runtime/src/operation",
    ],
    visibility = ["//visibility:public"],
)
