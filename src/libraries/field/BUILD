load("//tools/bzl:maven_artifact.bzl", "java_library_maven")

java_library_maven(
    name = "rpc",
    srcs = glob(["src/main/**/rpc/*.java"]),
    groupId = "se.tink.libraries.field",
    visibility = ["//visibility:public"],
    deps = [
        "@maven//:com_fasterxml_jackson_core_jackson_annotations",
        "@maven//:com_google_guava_guava",
        "@maven//:io_swagger_swagger_annotations",
    ],
)
