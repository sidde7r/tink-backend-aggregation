load("//tools/bzl:maven_artifact.bzl", "java_library_maven")

java_library_maven(
    name = "enums",
    srcs = glob(["src/main/**/enums/*.java"]),
    groupId = "se.tink.libraries.signableoperation",
    visibility = ["//visibility:public"],
)

java_library_maven(
    name = "rpc",
    srcs = glob(["src/main/**/rpc/*.java"]),
    groupId = "se.tink.libraries.signableoperation",
    visibility = ["//visibility:public"],
    deps = [
        "//src/libraries/generic_application",
        "//src/libraries/serialization",
        "//src/libraries/signableoperation:enums",
        "//src/libraries/transfer:rpc",
        "//src/libraries/user:rpc",
        "//src/libraries/uuid",
        "@maven//:com_fasterxml_jackson_core_jackson_annotations",
        "@maven//:com_google_guava_guava",
    ],
)
