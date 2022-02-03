load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "tls",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/lib",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/client",
        "//third_party:lombok_libs",
        "//third_party:org_slf4j_slf4j_api",
    ],
)
