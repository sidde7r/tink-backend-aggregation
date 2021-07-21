load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "agentclass",
    srcs = glob(
        ["*.java"],
    ),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/agent",
        "//third_party:com_google_inject_guice",
    ],
)
