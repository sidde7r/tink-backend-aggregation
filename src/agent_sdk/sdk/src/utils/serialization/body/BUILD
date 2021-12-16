load("//tools/bzl:lib_with_sources.bzl", "java_library_with_sources")

java_library_with_sources(
    name = "body",
    srcs = glob(["*.java"]),
    extdeps = [
        "@aggregation//:com_sun_jersey_jersey_client",
        "@aggregation//:com_sun_jersey_jersey_core",
        "@aggregation//:com_fasterxml_jackson_module_jackson_module_jaxb_annotations",
        "@aggregation//:com_fasterxml_jackson_core_jackson_core",
        "@aggregation//:com_fasterxml_jackson_core_jackson_databind",
        "@aggregation//:com_fasterxml_jackson_jaxrs_jackson_jaxrs_json_provider",
        "@aggregation//:com_fasterxml_jackson_jaxrs_jackson_jaxrs_base",
    ],
    visibility = ["//visibility:public"],
)
