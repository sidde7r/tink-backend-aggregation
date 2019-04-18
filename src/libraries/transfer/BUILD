load("//tools/bzl:maven_artifact.bzl", "java_library_maven")

java_library_maven(
    name = "enums",
    srcs = glob(["src/main/**/enums/*.java"]),
    groupId = "se.tink.libraries.transfer",
    visibility = ["//visibility:public"],
)

java_library_maven(
    name = "rpc",
    srcs = glob(["src/main/**/rpc/*.java"]),
    groupId = "se.tink.libraries.transfer",
    visibility = ["//visibility:public"],
    deps = [
        "//src/libraries/account_identifier",
        "//src/libraries/amount",
        "//src/libraries/date",
        "//src/libraries/serialization",
        "//src/libraries/transfer:enums",
        "//src/libraries/uuid",
        "@maven//:com_fasterxml_jackson_core_jackson_annotations",
        "@maven//:com_fasterxml_jackson_core_jackson_core",
        "@maven//:com_fasterxml_jackson_core_jackson_databind",
        "@maven//:com_google_guava_guava",
        "@maven//:org_slf4j_slf4j_api",
    ],
)

java_library_maven(
    name = "mocks",
    srcs = glob(["src/main/**/mocks/*.java"]),
    groupId = "se.tink.libraries.transfer",
    visibility = ["//visibility:public"],
    deps = [
        "//src/libraries/account_identifier",
        "//src/libraries/amount",
        "//src/libraries/transfer:enums",
        "//src/libraries/transfer:rpc",
        "@maven//:com_fasterxml_jackson_core_jackson_core",
        "@maven//:com_fasterxml_jackson_core_jackson_databind",
    ],
)
