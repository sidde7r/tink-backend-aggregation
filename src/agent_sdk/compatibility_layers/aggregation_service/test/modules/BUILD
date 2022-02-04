load("//tools/bzl:lib_with_sources.bzl", "java_library_with_sources")

java_library_with_sources(
    name = "modules",
    srcs = glob(["*.java"]),
    extdeps = [
        "@aggregation//:com_google_inject_guice",
    ],
    intdeps = [
        "//src/agent_sdk/sdk/src/utils",
        "//src/agent_sdk/compatibility_layers/aggregation_service/src/modules",
        "//src/agent_sdk/runtime/test/utils",
    ],
    visibility = ["//visibility:public"],
)
