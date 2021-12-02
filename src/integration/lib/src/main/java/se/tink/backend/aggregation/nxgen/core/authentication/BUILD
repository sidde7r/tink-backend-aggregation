load("//tools/bzl:lib_with_sources.bzl", "java_library_with_sources")

java_library_with_sources(
    name = "authentication",
    srcs = glob(["*.java"]),
    extdeps = [
        "//third_party:com_auth0_java_jwt",
        "//third_party:lombok_libs",
        "//third_party:org_apache_commons_commons_lang3",
        "//third_party:org_slf4j_slf4j_api",
        "@aggregation//:com_fasterxml_jackson_core_jackson_annotations",
        "@aggregation//:com_google_guava_guava",
    ],
    intdeps = [
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/header",
    ],
    visibility = ["//visibility:public"],
)
