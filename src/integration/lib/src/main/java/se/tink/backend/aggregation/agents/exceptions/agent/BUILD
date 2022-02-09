load("//tools/bzl:lib_with_sources.bzl", "java_library_with_sources")

java_library_with_sources(
    name = "agent",
    srcs = glob(["*.java"]),
    intdeps = [
        "//src/libraries/i18n_aggregation",
    ],
    visibility = ["//visibility:public"],
)
