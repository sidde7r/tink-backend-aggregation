load("//tools/bzl:lib_with_sources.bzl", "java_library_with_sources")

java_library_with_sources(
    name = "instance",
    srcs = glob(["*.java"]),
    extdeps = [
        "@aggregation//:com_google_guava_guava",
    ],
    intdeps = [
        "//src/agent_sdk/runtime/src/environment",
        "//src/agent_sdk/sdk/src/authentication/features",
        "//src/agent_sdk/sdk/src/payments/features/bulk",
    ],
    visibility = ["//visibility:public"],
    exports = ["//src/agent_sdk/runtime/src/environment"],
)
