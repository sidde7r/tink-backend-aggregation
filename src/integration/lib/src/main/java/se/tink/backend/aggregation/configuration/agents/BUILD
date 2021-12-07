load("//tools/bzl:lib_with_sources.bzl", "java_library_with_sources")

java_library_with_sources(
    name = "agents",
    srcs = glob(["*.java"]),
    extdeps = [
        "@aggregation//:com_fasterxml_jackson_core_jackson_annotations",
        "@aggregation//:com_google_guava_guava",
        "@aggregation//:com_kjetland_mbknor_jackson_jsonschema_2_12",
    ],
    intdeps = [
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/annotations",
    ],
    visibility = ["//visibility:public"],
)
