load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "instrumentation",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//third_party:lombok_libs",
    ],
)
