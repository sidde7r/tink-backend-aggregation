load("//tools/bzl:lib_with_sources.bzl", "java_library_with_sources")

java_library_with_sources(
    name = "qsealc",
    srcs = glob(["*.java"]),
    intdeps = [
        "//src/agent_sdk/sdk/src/utils/signer/signature",
    ],
    visibility = ["//visibility:public"],
)
