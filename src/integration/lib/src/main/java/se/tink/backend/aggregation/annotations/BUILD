load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "annotations",
    srcs = glob(["**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//third_party:com_google_inject_guice",
        "@aggregation//:com_fasterxml_jackson_core_jackson_annotations",
        "@aggregation//:com_fasterxml_jackson_core_jackson_core",
        "@aggregation//:com_fasterxml_jackson_core_jackson_databind",
        "@aggregation//:javax_inject_javax_inject",
    ],
)
