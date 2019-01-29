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
        "//src/libraries/date",
        "//src/libraries/field:rpc",
        "//src/libraries/provider:enums",
        "//src/libraries/serialization",
        "//third_party:com_fasterxml_jackson_core_jackson_annotations",
        "//third_party:com_github_rholder_guava_retrying",
        "//third_party:io_protostuff_protostuff_api",
        "//third_party:io_swagger_swagger_annotations",
        "//third_party:org_hibernate_hibernate_entitymanager",
        "//third_party:org_opensaml_opensaml",
    ],
)
