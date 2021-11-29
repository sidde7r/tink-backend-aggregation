load("@rules_java//java:defs.bzl", "java_binary", "java_library")

java_library(
    name = "example",
    srcs = glob(["src/**/*.java"]),
    visibility = [
        # TODO: update
        "//visibility:public",
    ],
    deps = [
        "//src/agent_sdk/sdk",
    ],
)
