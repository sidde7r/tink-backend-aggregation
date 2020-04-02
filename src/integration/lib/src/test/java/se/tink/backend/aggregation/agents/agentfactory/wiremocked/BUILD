load("//tools/bzl:junit.bzl", "junit_test")

java_library(
    name = "wiremocked",
    srcs = glob(["*.java"]),
    data = [],
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/lib",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/framework/wiremock/configuration",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/agents/componentproviders",
        "//third_party:com_google_inject_guice",
    ],
)
