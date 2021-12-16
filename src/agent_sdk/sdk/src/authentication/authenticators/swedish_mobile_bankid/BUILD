load("//tools/bzl:lib_with_sources.bzl", "java_library_with_sources")

java_library_with_sources(
    name = "swedish_mobile_bankid",
    srcs = glob(["**/*.java"]),
    extdeps = [
        "@aggregation//:com_google_code_findbugs_jsr305",
    ],
    intdeps = [
        "//src/agent_sdk/sdk/src/authentication/new_consent",
        "//src/agent_sdk/sdk/src/authentication/common_steps",
        "//src/agent_sdk/sdk/src/authentication/authenticators/thirdparty_app",
        "//src/agent_sdk/sdk/src/user_interaction",
        "//src/agent_sdk/sdk/src/storage",
    ],
    visibility = ["//visibility:public"],
)
