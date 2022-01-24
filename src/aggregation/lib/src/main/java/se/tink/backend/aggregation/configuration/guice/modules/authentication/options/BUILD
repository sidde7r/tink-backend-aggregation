load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "options",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/aggregation/api",
        "//src/aggregation/lib/src/main/java/se/tink/backend/aggregation/resources/authentication/options",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/authentication/options",
        "//src/libraries/jersey_guice",
        "//third_party:com_google_inject_guice",
        "//third_party:io_dropwizard_dropwizard_jersey",
        "//third_party:lombok_libs",
    ],
)
