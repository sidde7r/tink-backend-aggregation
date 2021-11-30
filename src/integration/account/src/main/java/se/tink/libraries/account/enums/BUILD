load("//tools/bzl:lib_with_sources.bzl", "java_library_with_sources")

java_library_with_sources(
    name = "enums",
    srcs = glob(["*.java"]),
    intdeps = [
        "//src/integration/enums",
    ],
    visibility = ["//src/integration:__subpackages__"],
)
