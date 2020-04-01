load("//tools/bzl:junit.bzl", "junit_test")

java_library(
    name = "module",
    srcs = glob(["*.java"]),
    data = [],
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/credentials:service",
        "//src/integration/lib",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/framework/wiremock/configuration",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/configuration/agentsservice",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/configuration/signaturekeypair",
        "//third_party:com_google_inject_guice",
        "@aggregation//:com_google_guava_guava",
    ],
)
