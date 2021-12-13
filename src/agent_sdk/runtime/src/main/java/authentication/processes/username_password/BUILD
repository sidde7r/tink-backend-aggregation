load("//tools/bzl:lib_with_sources.bzl", "java_library_with_sources")

java_library_with_sources(
    name = "username_password",
    srcs = glob(["*.java"]),
    extdeps = [
        "@agent_sdk_maven//:com_google_guava_guava",
        "@agent_sdk_maven//:org_slf4j_slf4j_api",
    ],
    intdeps = [
        "//src/agent_sdk/runtime/src/main/java/authentication/processes",
        "//src/agent_sdk/runtime/src/main/java/instance",
        "//src/agent_sdk/sdk",
    ],
    visibility = [
        "//src/agent_sdk/runtime:__subpackages__",
    ],
)
