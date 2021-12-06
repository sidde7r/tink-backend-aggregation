load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "eidas",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/libraries/cryptography/parser",
        "@aggregation//:com_google_guava_guava",
        "@tink_backend//src/eidas-dev-issuer/client",
    ],
)
