load("@rules_java//java:defs.bzl", "java_library")
load("//tools/bzl:junit.bzl", "junit_test")

java_library(
    name = "fakelogmasker",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/logmasker",
        "@aggregation//:com_google_guava_guava",
        "@aggregation//:io_reactivex_rxjava3_rxjava",
    ],
)
