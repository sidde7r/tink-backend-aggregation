load("//tools/bzl:lib_with_sources.bzl", "java_library_with_sources")

java_library_with_sources(
    name = "aggregator_configuration",
    srcs = glob(["*.java"]),
    extdeps = [
        "@aggregation//:com_kjetland_mbknor_jackson_jsonschema_2_12",
    ],
    intdeps = [
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/configuration/agents",
    ],
    visibility = ["//visibility:public"],
    exports = [
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/configuration/agents",
        "@aggregation//:com_kjetland_mbknor_jackson_jsonschema_2_12",
    ],
)
