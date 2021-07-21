load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "draining",
    srcs = glob(["src/main/**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//third_party:com_google_inject_guice",
        "//third_party:io_dropwizard_dropwizard_core",
        "//third_party:io_dropwizard_dropwizard_servlets",
        "@aggregation//:com_google_guava_guava",
    ],
)
