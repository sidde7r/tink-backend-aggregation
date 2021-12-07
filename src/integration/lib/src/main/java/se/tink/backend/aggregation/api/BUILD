load("@rules_java//java:defs.bzl", "java_library")
load("//tools/bzl:lib_with_sources.bzl", "java_library_with_sources")

java_library_with_sources(
    name = "api",
    srcs = ["AggregatorInfo.java"],
    intdeps = [
        ":psd2_headers",
    ],
    visibility = ["//visibility:public"],
    exports = [
        ":psd2_headers",
    ],
)

java_library_with_sources(
    name = "psd2_headers",
    srcs = ["Psd2Headers.java"],
    intdeps = [
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/configuration/agents/utils",
        "//src/libraries/cryptography/hash",
    ],
    visibility = ["//visibility:public"],
)
