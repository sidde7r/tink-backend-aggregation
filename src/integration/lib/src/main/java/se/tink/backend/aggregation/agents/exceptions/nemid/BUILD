load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "nemid",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/exceptions",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/exceptions/agent",
        "//src/libraries/i18n_aggregation",
    ],
)
