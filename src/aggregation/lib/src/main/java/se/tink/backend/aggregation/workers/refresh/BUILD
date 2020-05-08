load("@tink_backend//tools/bzl:junit_strict.bzl", "junit_test")

java_library(
    name = "refresh",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/credentials/src/main/java/se/tink/libraries/credentials/service",
        "//third_party:com_google_guava_guava",
    ],
)
