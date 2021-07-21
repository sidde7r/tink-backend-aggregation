load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "qrcode",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//third_party:com_google_http_client_google_http_client",
        "//third_party:zxing-qrcode",
        "@aggregation//:com_google_guava_guava",
    ],
)
