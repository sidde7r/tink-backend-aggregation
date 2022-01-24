load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "options",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/aggregation/api",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/authentication/options",
        "//third_party:com_google_inject_guice",
        "//third_party:lombok_libs",
        "@tink_backend//src/libraries/authentication_options",
    ],
)
