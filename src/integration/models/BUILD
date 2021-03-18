java_library(
    name = "models",
    srcs = glob(["src/main/**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/amount",
        "//src/libraries/serialization",
        "//src/libraries/strings",
        "//src/libraries/uuid",
        "@aggregation//:com_fasterxml_jackson_core_jackson_annotations",
        "@aggregation//:com_fasterxml_jackson_core_jackson_core",
        "@aggregation//:com_fasterxml_jackson_core_jackson_databind",
        "@aggregation//:com_fasterxml_uuid_java_uuid_generator",
        "@aggregation//:com_google_guava_guava",
        "@tink_backend//src/libraries/account_identifier",
    ],
)
