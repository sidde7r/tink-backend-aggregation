load("//tools/bzl:lib_with_sources.bzl", "java_library_with_sources")

java_library_with_sources(
    name = "swedish_mobile_bankid",
    srcs = glob(["*.java"]),
    extdeps = [
        "@aggregation//:com_google_guava_guava",
        "@aggregation//:org_slf4j_slf4j_api",
    ],
    intdeps = [
        "//src/agent_sdk/runtime/src/authentication/processes",
        "//src/agent_sdk/runtime/src/instance",
        "//src/agent_sdk/sdk",
    ],
    visibility = [
        "//src/agent_sdk/runtime:__subpackages__",
    ],
)
