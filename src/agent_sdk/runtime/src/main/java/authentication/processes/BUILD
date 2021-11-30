load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "processes",
    srcs = glob(["*.java"]),
    visibility = [
        "//src/agent_sdk/runtime:__subpackages__",
    ],
    deps = [
        "//src/agent_sdk/runtime/src/main/java/instance",
        "//src/agent_sdk/sdk",
        "@agent_sdk_maven//:com_google_guava_guava",
    ],
)
