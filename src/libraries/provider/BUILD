load("//tools/bzl:maven_artifact.bzl", "java_library_maven")

java_library_maven(
    name = "enums",
    srcs = glob(["src/main/**/enums/*.java"]),
    groupId = "se.tink.libraries.provider",
    visibility = ["//visibility:public"],
)

java_library_maven(
    name = "rpc",
    srcs = glob(["src/main/**/rpc/*.java"]),
    groupId = "se.tink.libraries.provider",
    visibility = ["//visibility:public"],
    deps = [
        "//src/libraries/date",
        "@maven//:com_fasterxml_jackson_core_jackson_annotations",
        "@maven//:com_google_guava_guava",
        "@maven//:joda_time_joda_time",
    ],
)
