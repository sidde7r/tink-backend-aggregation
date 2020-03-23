load("//tools/bzl:junit.bzl", "junit_test")

java_library(
    name = "production",
    srcs = glob(["*.java"]),
    data = [],
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/lib",
        "//third_party:com_google_inject_guice",
    ],
)
