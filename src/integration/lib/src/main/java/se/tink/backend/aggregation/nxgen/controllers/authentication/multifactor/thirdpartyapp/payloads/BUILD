load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "payloads",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/url",
        "//third_party:com_google_guava_guava",
        "//third_party:lombok_libs",
    ],
)
