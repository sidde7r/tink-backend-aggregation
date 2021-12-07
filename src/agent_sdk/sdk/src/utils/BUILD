load("//tools/bzl:lib_with_sources.bzl", "java_library_with_sources")

java_library_with_sources(
    name = "utils",
    srcs = glob(["*.java"]),
    intdeps = [
        "//src/agent_sdk/sdk/src/utils/serialization",
    ],
    visibility = ["//visibility:public"],
    exports = [
        "//src/agent_sdk/sdk/src/utils/serialization",
    ],
)
