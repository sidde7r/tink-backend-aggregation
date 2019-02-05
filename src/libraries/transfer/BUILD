java_library(
    name = "enums",
    srcs = glob(["src/main/**/enums/*.java"]),
    visibility = ["//visibility:public"],
    deps = ["//src/libraries/account_identifier"],
)

java_library(
    name = "rpc",
    srcs = glob(["src/main/**/rpc/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/libraries/account:enums",
        "//src/libraries/account_identifier",
        "//src/libraries/amount",
        "//src/libraries/date",
        "//src/libraries/enums",
        "//src/libraries/serialization",
        "//src/libraries/transfer:enums",
        "//src/libraries/uuid",
        "//third_party:io_dropwizard_dropwizard_jackson",
        "//third_party:io_dropwizard_dropwizard_servlets",
        "//third_party:io_protostuff_protostuff_api",
        "//third_party:io_swagger_swagger_annotations",
        "//third_party:org_apache_curator_curator_x_discovery",
        "//third_party:org_hibernate_hibernate_entitymanager",
    ],
)

java_library(
    name = "mocks",
    srcs = glob(["src/main/**/mocks/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/libraries/account_identifier",
        "//src/libraries/amount",
        "//src/libraries/enums",
        "//src/libraries/transfer:enums",
        "//src/libraries/transfer:rpc",
        "//third_party:com_fasterxml_jackson_core_jackson_core",
        "//third_party:com_fasterxml_jackson_core_jackson_databind",
    ],
)
