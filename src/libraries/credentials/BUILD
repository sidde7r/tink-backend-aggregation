java_library(
    name = "enums",
    srcs = glob(["src/main/**/enums/*.java"]),
    visibility = ["//visibility:public"],
)

java_library(
    name = "rpc",
    srcs = glob(["src/main/**/rpc/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/libraries/credentials:enums",
        "//src/libraries/field:rpc",
        "//src/libraries/provider:rpc",
        "//src/libraries/serialization",
        "//src/libraries/strings",
        "//third_party:com_fasterxml_jackson_core_jackson_annotations",
        "//third_party:com_fasterxml_jackson_core_jackson_core",
        "//third_party:com_google_guava_guava",
    ],
)

java_library(
    name = "service",
    srcs = glob(["src/main/**/service/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/api",
        "//src/libraries/strings",
        "//src/libraries/user:rpc",
        "//third_party:com_fasterxml_jackson_core_jackson_databind",
        "//third_party:com_google_guava_guava",
        "//third_party:commons_codec_commons_codec",
    ],
)
