load("@rules_java//java:defs.bzl", "java_library")

# Additional classes taken from newer versions of Dropwizard
java_library(
    name = "dropwizard",
    srcs = glob(["src/main/java/**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//third_party:io_dropwizard_dropwizard_configuration",
        "@aggregation//:org_apache_commons_commons_text",
    ],
)
