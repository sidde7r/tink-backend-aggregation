load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "validator",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/aggregation/api",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/tools",
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//third_party:com_google_guava_guava",
    ],
)
