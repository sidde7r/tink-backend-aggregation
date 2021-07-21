load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "queue_lib",
    srcs = glob(["src/main/**/*.java"]),
    resources = glob(["src/main/resources/**"]),
    visibility = ["//visibility:public"],
    deps = [
        "//third_party:io_dropwizard_dropwizard_lifecycle",
        "@aggregation//:com_google_guava_guava",
    ],
)
