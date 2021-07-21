load("@rules_java//java:defs.bzl", "java_library")
load("//tools/bzl:junit.bzl", "junit_test")

java_library(
    name = "wiremocked",
    srcs = glob(["**/*.java"]),
    data = [],
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/agentfactory",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/agentfactory/iface",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/framework/wiremock/configuration",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/framework/wiremock/configuration/provider/socket",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/framework/wiremock/module",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/configuration/agentsservice",
        "//third_party:com_google_inject_guice",
    ],
)
