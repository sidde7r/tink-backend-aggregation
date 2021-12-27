load("//tools/bzl:lib_with_sources.bzl", "java_library_with_sources")

java_library_with_sources(
    name = "proxy",
    srcs = glob(["*.java"]),
    extdeps = [
        "@aggregation//:com_google_code_findbugs_jsr305",
    ],
    visibility = ["//visibility:public"],
)
