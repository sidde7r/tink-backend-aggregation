load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "mocks",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//third_party:org_mockito_mockito_core",
    ],
)
