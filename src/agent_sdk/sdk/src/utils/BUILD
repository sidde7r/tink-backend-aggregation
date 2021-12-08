load("//tools/bzl:lib_with_sources.bzl", "java_library_with_sources")

java_library_with_sources(
    name = "utils",
    srcs = glob(["*.java"]),
    intdeps = [
        "//src/agent_sdk/sdk/src/models/field",
        "//src/agent_sdk/sdk/src/utils/serialization",
        "//src/agent_sdk/sdk/src/exceptions",
        "//src/agent_sdk/sdk/src/user_interaction",
    ],
    visibility = ["//visibility:public"],
    exports = [
        "//src/agent_sdk/sdk/src/utils/serialization",
    ],
)
