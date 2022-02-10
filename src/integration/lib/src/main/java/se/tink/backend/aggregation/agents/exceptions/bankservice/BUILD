load("//tools/bzl:lib_with_sources.bzl", "java_library_with_sources")

java_library_with_sources(
    name = "bankservice",
    srcs = glob(["*.java"]),
    intdeps = [
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/exceptions/agent",
        "//src/libraries/i18n_aggregation",
    ],
    visibility = ["//visibility:public"],
)
