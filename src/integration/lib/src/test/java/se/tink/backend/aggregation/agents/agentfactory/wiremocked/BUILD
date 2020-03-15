load("//tools/bzl:junit.bzl", "junit_test")

java_library(
    name = "wiremocked",
    srcs = glob(["*.java"]),
    data = [],
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/lib",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework/wiremock/configuration",
        "//third_party:com_google_inject_guice",
    ],
)
