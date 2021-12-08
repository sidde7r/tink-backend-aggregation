load("//tools/bzl:lib_with_sources.bzl", "java_library_with_sources")

java_library_with_sources(
    name = "authenticators",
    intdeps = [
        "//src/agent_sdk/sdk/src/authentication/authenticators/generic",
        "//src/agent_sdk/sdk/src/authentication/authenticators/oauth2",
        "//src/agent_sdk/sdk/src/authentication/authenticators/oauth2_decoupled_app",
        "//src/agent_sdk/sdk/src/authentication/authenticators/thirdparty_app",
        "//src/agent_sdk/sdk/src/authentication/authenticators/username_password",
        "//src/agent_sdk/sdk/src/authentication/authenticators/swedish_mobile_bankid",
    ],
    visibility = ["//visibility:public"],
    exports = [
        "//src/agent_sdk/sdk/src/authentication/authenticators/generic",
        "//src/agent_sdk/sdk/src/authentication/authenticators/oauth2",
        "//src/agent_sdk/sdk/src/authentication/authenticators/oauth2_decoupled_app",
        "//src/agent_sdk/sdk/src/authentication/authenticators/swedish_mobile_bankid",
        "//src/agent_sdk/sdk/src/authentication/authenticators/thirdparty_app",
        "//src/agent_sdk/sdk/src/authentication/authenticators/username_password",
    ],
)
