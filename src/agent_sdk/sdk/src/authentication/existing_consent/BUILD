load("//tools/bzl:lib_with_sources.bzl", "java_library_with_sources")

java_library_with_sources(
    name = "existing_consent",
    srcs = glob(["*.java"]),
    extdeps = [
        "@aggregation//:com_google_guava_guava",
        "@aggregation//:com_google_code_findbugs_jsr305",
        "//src/agent_sdk:lombok_libs",
    ],
    intdeps = [
        "//src/agent_sdk/sdk/src/storage",
        "//src/agent_sdk/sdk/src/operation",
    ],
    visibility = ["//visibility:public"],
)
