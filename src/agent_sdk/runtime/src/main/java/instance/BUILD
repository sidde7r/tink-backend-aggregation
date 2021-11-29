load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "instance",
    srcs = glob(["*.java"]),
    visibility = [
        "//src/agent_sdk/runtime:__subpackages__",
    ],
    deps = [
        "//src/agent_sdk/sdk",
        "@agent_sdk_maven//:com_google_guava_guava",
        "@agent_sdk_maven//:com_google_inject_guice",
        "@agent_sdk_maven//:org_slf4j_slf4j_api",
    ],
)
