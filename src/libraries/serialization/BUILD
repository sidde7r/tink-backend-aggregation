load("//tools/bzl:maven_artifact.bzl", "java_library_maven")

java_library_maven(
    name = "serialization",
    srcs = glob(["src/main/**/*.java"]),
    groupId = "se.tink.libraries.serialization",
    visibility = ["//visibility:public"],
    deps = [
        # Log
        "@maven//:org_slf4j_slf4j_api",

        # Jackson
        "@maven//:com_fasterxml_jackson_core_jackson_core",
        "@maven//:com_fasterxml_jackson_core_jackson_databind",
        "@maven//:com_fasterxml_jackson_dataformat_jackson_dataformat_smile",

        # Codec
        "@maven//:commons_codec_commons_codec",
    ],
)
