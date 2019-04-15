load("//tools/bzl:maven_artifact.bzl", "java_library_maven")

java_library_maven(
    name = "serialization",
    srcs = glob(["src/main/**/*.java"]),
    groupId = "se.tink.libraries.serialization",
    visibility = ["//visibility:public"],
    deps = [
        # Log
        "//third_party:org_slf4j_slf4j_api",

        # Jackson
        "//third_party:com_fasterxml_jackson_core_jackson_core",
        "//third_party:com_fasterxml_jackson_core_jackson_databind",
        "//third_party:com_fasterxml_jackson_dataformat_jackson_dataformat_smile",

        # Codec
        "//third_party:commons_codec_commons_codec",
    ],
)
