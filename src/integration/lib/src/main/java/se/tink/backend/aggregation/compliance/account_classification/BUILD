load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "account_classification",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/libraries/metrics:core",
        "//src/libraries/metrics:registry",
    ],
)
