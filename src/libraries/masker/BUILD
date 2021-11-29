load("//tools/bzl:junit.bzl", "junit_test")
load("//tools/bzl:lib_with_sources.bzl", "java_library_with_sources")

java_library_with_sources(
    name = "masker",
    srcs = glob(
        ["*.java"],
        exclude = [
            "*Test.java",
        ],
    ),
    extdeps = [
        "@aggregation//:com_google_guava_guava",
    ],
    intdeps = [
        "//src/libraries/cryptography/hash",
    ],
    visibility = ["//visibility:public"],
)

junit_test(
    name = "masker_test",
    srcs = glob(["*Test.java"]),
    deps = [
        ":masker",
        "//third_party:org_assertj_assertj_core",
    ],
)
