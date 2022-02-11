load("//tools/bzl:lib_with_sources.bzl", "java_library_with_sources")

java_library_with_sources(
    name = "authentication",
    srcs = glob(["*.java"]),
    extdeps = [
        "@aggregation//:com_google_guava_guava",
    ],
    intdeps = [
        "//src/agent_sdk/runtime/src/authentication/processes",
        "//src/agent_sdk/runtime/src/authentication/processes/berlingroup",
        "//src/agent_sdk/runtime/src/authentication/processes/generic",
        "//src/agent_sdk/runtime/src/authentication/processes/oauth2",
        "//src/agent_sdk/runtime/src/authentication/processes/oauth2_decoupled_app",
        "//src/agent_sdk/runtime/src/authentication/processes/oauth2_decoupled_swedish_mobile_bankid",
        "//src/agent_sdk/runtime/src/authentication/processes/swedish_mobile_bankid",
        "//src/agent_sdk/runtime/src/authentication/processes/thirdparty_app",
        "//src/agent_sdk/runtime/src/authentication/processes/username_password",
        "//src/agent_sdk/runtime/src/instance",
        "//src/agent_sdk/sdk",
    ],
    visibility = [
        "//src/agent_sdk/compatibility_layers:__subpackages__",
        "//src/agent_sdk/runtime:__subpackages__",
    ],
)
