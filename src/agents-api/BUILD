package(default_visibility = ["//visibility:public"])

java_library(
    name = "agents-api",
    srcs = glob(["src/main/**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/libraries/account:enums",
        "//src/libraries/account_identifier:account-identifier",
        "//src/libraries/demo_credentials:demo-credentials",
        "//src/libraries/enums",
        "//src/libraries/field:rpc",
        "//src/libraries/serialization",
        "//src/libraries/strings",
        "//src/libraries/user:rpc",
        "//src/libraries/uuid",
        "//third_party:com_fasterxml_jackson_core_jackson_annotations",
        "//third_party:com_fasterxml_jackson_core_jackson_core",
        "//third_party:com_google_guava_guava",
    ],
)
