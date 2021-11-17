load("@rules_java//java:defs.bzl", "java_library")
load("//tools/bzl:junit.bzl", "junit_test")

java_library(
    name = "hash",
    srcs = glob(
        ["*.java"],
        exclude = [
            "*Test.java",
        ],
    ),
    visibility = ["//visibility:public"],
    deps = [
        "//third_party:commons_codec_commons_codec",
    ],
)

junit_test(
    name = "hash_test",
    srcs = glob(["*Test.java"]),
    deps = [
        "//src/libraries/cryptography/hash",
        "//src/libraries/encoding",
        "//third_party:junit_junit",
    ],
)
