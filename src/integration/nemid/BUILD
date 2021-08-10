load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "nemid",
    srcs = glob(["**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/lib",
        "//third_party:com_googlecode_gettext_commons_gettext_commons",
        "//third_party:lombok_libs",
    ],
)
