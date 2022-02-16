load("//tools/bzl:lib_with_sources.bzl", "java_library_with_sources")

java_library_with_sources(
    name = "accounts",
    srcs = glob(["*.java"]),
    extdeps = [
        "//src/agent_sdk/sdk/src/models/transaction",
    ],
    intdeps = [
        "//src/agent_sdk/sdk/src/models/account",
    ],
    visibility = ["//src/agent_sdk:__subpackages__"],
)
