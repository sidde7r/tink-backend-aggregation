load("//tools/bzl:lib_with_sources.bzl", "java_library_with_sources")

java_library_with_sources(
    name = "thirdparty_app",
    srcs = glob(["**/*.java"]),
    extdeps = [
        "@agent_sdk_maven//:com_google_code_findbugs_jsr305",
        "@agent_sdk_maven//:org_projectlombok_lombok",
        "//src/agent_sdk:lombok_plugin",
    ],
    intdeps = [
        "//src/agent_sdk/sdk/src/authentication/common_steps",
        "//src/agent_sdk/sdk/src/utils",
        "//src/agent_sdk/sdk/src/authentication/new_consent",
        "//src/agent_sdk/sdk/src/storage",
        "//src/agent_sdk/sdk/src/user_interaction",
        "//src/agent_sdk/sdk/src/exceptions",
    ],
    visibility = ["//visibility:public"],
)
