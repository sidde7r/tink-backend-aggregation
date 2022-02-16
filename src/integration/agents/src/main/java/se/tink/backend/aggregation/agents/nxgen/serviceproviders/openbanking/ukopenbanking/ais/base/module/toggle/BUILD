load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "toggle",
    srcs = glob(["**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/lib",
        "//third_party:com_google_inject_guice",
        "//third_party:lombok_libs",
        "//third_party:org_slf4j_slf4j_api",
        "@aggregation//:no_finn_unleash_unleash_client_java",
        "@tink_backend//src/libraries/unleash",
    ],
)
