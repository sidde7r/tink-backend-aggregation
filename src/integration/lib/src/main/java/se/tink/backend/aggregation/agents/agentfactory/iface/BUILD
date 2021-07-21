load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "iface",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    runtime_deps = [
        "//po:i18n",
    ],
    deps = [
        "//src/integration/credentials/src/main/java/se/tink/libraries/credentials/service",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/agent",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/contexts/agent",
    ],
)
