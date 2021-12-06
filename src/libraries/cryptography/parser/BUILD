load("//tools/bzl:lib_with_sources.bzl", "java_library_with_sources")

java_library_with_sources(
    name = "parser",
    srcs = glob(["*.java"]),
    extdeps = [
        "//third_party:org_bouncycastle_bcpkix_jdk15on",
    ],
    visibility = ["//visibility:public"],
)
