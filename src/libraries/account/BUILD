java_library(
    name = "enums",
    srcs = glob(["src/main/**/enums/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/libraries/enums",
    ],
)

java_library(
    name = "rpc",
    srcs = glob(["src/main/**/rpc/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/libraries/account:enums",
        "//src/libraries/account_identifier",
        "//src/libraries/serialization",
        "//src/libraries/strings",
        "//src/libraries/transfer:rpc",
        "//third_party:com_fasterxml_jackson_core_jackson_databind",
        "//third_party:com_github_rholder_guava_retrying",
        "//third_party:io_protostuff_protostuff_api",
        "//third_party:io_swagger_swagger_annotations",
        "//third_party:org_hibernate_javax_persistence_hibernate_jpa_2_0_api",
    ],
)
