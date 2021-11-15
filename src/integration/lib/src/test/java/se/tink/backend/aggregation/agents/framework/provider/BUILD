load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "provider",
    srcs = glob(["*.java"]),
    data = [],
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/configuration",
        "@aggregation//:com_fasterxml_jackson_core_jackson_databind",
    ],
)
