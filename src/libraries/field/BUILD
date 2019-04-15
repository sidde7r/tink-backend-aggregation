load("//tools/bzl:maven_artifact.bzl", "java_library_maven")

java_library_maven(
    name = "rpc",
    srcs = glob(["src/main/**/rpc/*.java"]),
    groupId = "se.tink.libraries.field",
    visibility = ["//visibility:public"],
    deps = [
        "//third_party:com_fasterxml_jackson_core_jackson_annotations",
        "//third_party:com_google_guava_guava",
        "//third_party:io_swagger_swagger_annotations",
    ],
)
