load("//tools/bzl:lib_with_sources.bzl", "java_library_with_sources")

java_library_with_sources(
    name = "generic",
    srcs = glob(["**/*.java"]),
    extdeps = [
        "@agent_sdk_maven//:com_google_guava_guava",
        "@agent_sdk_maven//:com_google_code_findbugs_jsr305",
    ],
    intdeps = [
        "//src/agent_sdk/sdk/src/authentication/new_consent",
        "//src/agent_sdk/sdk/src/authentication/existing_consent",
    ],
    visibility = ["//visibility:public"],
)
