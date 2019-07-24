load("//tools/bzl:maven_artifact.bzl", "java_library_maven")

java_library_maven(
    name = "document_container",
    srcs = glob(["src/main/**/*.java"]),
    groupId = "se.tink.libraries.documentcontainer",
    visibility = ["//visibility:public"],
    deps = [
        "@maven//:com_fasterxml_jackson_core_jackson_annotations",
        "@maven//:com_sun_jersey_jersey_core",
        "@maven//:commons_io_commons_io",
    ],
)
