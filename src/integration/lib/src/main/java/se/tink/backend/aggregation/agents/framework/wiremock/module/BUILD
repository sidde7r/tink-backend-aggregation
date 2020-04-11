load("//tools/bzl:junit.bzl", "junit_test")

java_library(
    name = "module",
    srcs = glob(["*.java"]),
    data = [],
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/credentials:service",
        "//src/integration/lib",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/agentfactory",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/contexts/agent",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/framework/wiremock/configuration",
        "//third_party:com_google_inject_guice",
        "@aggregation//:com_google_guava_guava",
    ],
)
