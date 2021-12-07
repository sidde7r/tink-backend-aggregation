load("//tools/bzl:lib_with_sources.bzl", "java_library_with_sources")

java_library_with_sources(
    name = "utils",
    srcs = glob(["*.java"]),
    extdeps = [
        "//third_party:com_google_code_gson_gson",
        "//third_party:lombok_libs",
        "//third_party:org_apache_commons_commons_collections4",
        "//third_party:org_bouncycastle_bcpkix_jdk15on",
        "//third_party:org_json_json",
        "@aggregation//:com_fasterxml_jackson_core_jackson_annotations",
        "@aggregation//:com_google_guava_guava",
    ],
    intdeps = [
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/annotations",
    ],
    visibility = ["//visibility:public"],
)
