load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "signaturekeypair",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/libraries/cryptography",
        "//third_party:com_fasterxml_jackson_core_jackson_annotations",
        "//third_party:com_google_guava_guava",
        "//third_party:commons_codec_commons_codec",
        "//third_party:org_slf4j_slf4j_api",
    ],
)
