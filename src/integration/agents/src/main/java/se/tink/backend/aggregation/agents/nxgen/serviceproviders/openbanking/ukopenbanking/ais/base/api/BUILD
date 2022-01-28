load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "api",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/lib",
        "//src/libraries/strings",
        "//third_party:lombok_libs",
        "//third_party:org_slf4j_slf4j_api",
        "@aggregation//:com_fasterxml_jackson_core_jackson_annotations",
        "@aggregation//:com_google_guava_guava",
    ],
)
