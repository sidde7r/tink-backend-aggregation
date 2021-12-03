load("//tools/bzl:lib_with_sources.bzl", "java_library_with_sources")

java_library_with_sources(
    name = "annotations",
    srcs = glob(["**/*.java"]),
    extdeps = [
        "//third_party:com_google_inject_guice",
        "@aggregation//:com_fasterxml_jackson_core_jackson_annotations",
        "@aggregation//:com_fasterxml_jackson_core_jackson_core",
        "@aggregation//:com_fasterxml_jackson_core_jackson_databind",
        "@aggregation//:javax_inject_javax_inject",
    ],
    visibility = ["//visibility:public"],
)
