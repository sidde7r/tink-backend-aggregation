load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "typemappers",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/lib",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/utils/log",
        "//src/libraries/pair",
        "//third_party:org_slf4j_slf4j_api",
    ],
)
