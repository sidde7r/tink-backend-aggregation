java_library(
    name = "enums",
    srcs = glob(["src/main/**/enums/*.java"]),
    visibility = ["//visibility:public"],
    deps = ["//src/libraries/account_identifier:account-identifier"],
)

java_library(
    name = "rpc",
    srcs = glob(["src/main/**/rpc/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/libraries/amount",
        "//src/libraries/account_identifier:account-identifier",
        "//src/libraries/date",
        "//src/libraries/enums",
        "//src/libraries/serialization",
        "//src/libraries/uuid",
        "//src/libraries/transfer:enums",
        "//third_party:io_dropwizard_dropwizard_jackson",
        "//third_party:io_dropwizard_dropwizard_servlets",
        "//third_party:io_protostuff_protostuff_api",
        "//third_party:io_swagger_swagger_annotations",
        "//third_party:org_apache_curator_curator_x_discovery",
        "//third_party:org_hibernate_hibernate_entitymanager",
    ],
)
