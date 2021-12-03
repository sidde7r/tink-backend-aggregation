load("//tools/bzl:lib_with_sources.bzl", "java_library_with_sources")

java_library_with_sources(
    name = "common_steps",
    srcs = glob(["*.java"]),
    extdeps = [
        "@agent_sdk_maven//:com_google_guava_guava",
    ],
    intdeps = [
        "//src/agent_sdk/sdk/src/authentication/new_consent",
        "//src/agent_sdk/sdk/src/authentication/existing_consent",
        "//src/agent_sdk/sdk/src/user_interaction",
        "//src/agent_sdk/sdk/src/models/field",
    ],
    visibility = ["//visibility:public"],
)
