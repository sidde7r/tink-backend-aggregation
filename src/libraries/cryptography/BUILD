load("@rules_java//java:defs.bzl", "java_library")
load("//tools/bzl:junit.bzl", "junit_test")

java_library(
    name = "cryptography",
    srcs = glob(
        ["*.java"],
        exclude = [
            "*Test.java",
        ],
    ),
    visibility = ["//visibility:public"],
    deps = [
        "//third_party:org_bouncycastle_bcpkix_jdk15on",
    ],
)

junit_test(
    name = "cryptography_test",
    srcs = glob(["*Test.java"]),
    data = [
        "//data:cryptography_test",
    ],
    deps = [
        "//src/libraries/cryptography",
        "//third_party:junit_junit",
        "//third_party:org_assertj_assertj_core",
        "//third_party:org_bouncycastle_bcpkix_jdk15on",
    ],
)
