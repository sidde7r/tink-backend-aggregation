load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "agent",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/configuration/agentsservice",
    ],
)
