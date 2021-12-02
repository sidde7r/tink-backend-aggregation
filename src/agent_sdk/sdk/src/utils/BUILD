load("//tools/bzl:lib_with_sources.bzl", "java_library_with_sources")

java_library_with_sources(
    name = "utils",
    srcs = glob(["*.java"]),
    extdeps = [
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/client",
    ],
    intdeps = [
    ],
    visibility = ["//visibility:public"],
)
