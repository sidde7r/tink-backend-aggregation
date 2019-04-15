load("//tools/bzl:maven_artifact.bzl", "java_library_maven")

java_library_maven(
    name = "rpc",
    srcs = glob(["src/main/**/rpc/*.java"]),
    groupId = "se.tink.libraries.user",
    visibility = ["//visibility:public"],
    deps = [
        "//src/libraries/serialization",
        "//src/libraries/strings",
        "//third_party:com_fasterxml_jackson_core_jackson_annotations",
        "//third_party:com_google_guava_guava",
    ],
)
