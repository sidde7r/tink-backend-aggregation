load("//tools/bzl:lib_with_sources.bzl", "java_library_with_sources")

java_library_with_sources(
    name = "authentication",
    extdeps = [
        "@agent_sdk_maven//:com_google_guava_guava",
    ],
    intdeps = [
        "//src/agent_sdk/runtime/src/main/java/authentication/processes",
        "//src/agent_sdk/runtime/src/main/java/authentication/processes/berlingroup",
        "//src/agent_sdk/runtime/src/main/java/authentication/processes/generic",
        "//src/agent_sdk/runtime/src/main/java/authentication/processes/oauth2",
        "//src/agent_sdk/runtime/src/main/java/authentication/processes/oauth2_decoupled_app",
        "//src/agent_sdk/runtime/src/main/java/authentication/processes/oauth2_decoupled_swedish_mobile_bankid",
        "//src/agent_sdk/runtime/src/main/java/authentication/processes/swedish_mobile_bankid",
        "//src/agent_sdk/runtime/src/main/java/authentication/processes/thirdparty_app",
        "//src/agent_sdk/runtime/src/main/java/authentication/processes/username_password",
        "//src/agent_sdk/runtime/src/main/java/instance",
        "//src/agent_sdk/sdk",
    ],
    visibility = ["//src/agent_sdk/runtime:__subpackages__"],
)
