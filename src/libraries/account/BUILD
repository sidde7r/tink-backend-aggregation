load("//tools/bzl:maven_artifact.bzl", "java_library_maven")

java_library_maven(
    name = "enums",
    srcs = glob(["src/main/**/enums/*.java"]),
    groupId = "se.tink.libraries.account",
    visibility = ["//visibility:public"],
    deps = [
        "//src/libraries/enums",
    ],
)

java_library_maven(
    name = "rpc",
    srcs = glob(["src/main/**/rpc/*.java"]),
    groupId = "se.tink.libraries.account",
    visibility = ["//visibility:public"],
    deps = [
        "//src/libraries/account:enums",
        "//src/libraries/account_identifier",
        "//src/libraries/serialization",
        "//src/libraries/strings",
        "//src/libraries/transfer:rpc",
        "//third_party:com_fasterxml_jackson_core_jackson_annotations",
        "//third_party:com_google_guava_guava",
    ],
)
