load("//tools/bzl:lib_with_sources.bzl", "java_library_with_sources")

java_library_with_sources(
    name = "url",
    srcs = glob(["*.java"]),
    extdeps = [
        "//third_party:com_sun_jersey_jersey_core",
        "//third_party:org_apache_commons_commons_lang3",
        "@aggregation//:com_google_guava_guava",
    ],
    visibility = ["//visibility:public"],
)
