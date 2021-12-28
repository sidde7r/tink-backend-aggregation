load("//tools/bzl:lib_with_sources.bzl", "java_library_with_sources")

java_library_with_sources(
    name = "http",
    srcs = glob(["*.java"]),
    intdeps = [
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/proxy",
    ],
    visibility = ["//visibility:public"],
)
