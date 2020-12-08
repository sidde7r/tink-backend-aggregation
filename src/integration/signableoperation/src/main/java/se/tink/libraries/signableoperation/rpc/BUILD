java_library(
    name = "rpc",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/signableoperation/src/main/java/se/tink/libraries/signableoperation/enums",
        "//src/integration/transfer:rpc",
        "//src/libraries/jersey_utils",
        "//src/libraries/serialization",
        "//src/libraries/uuid",
        "@aggregation//:com_fasterxml_jackson_core_jackson_annotations",
        "@aggregation//:com_google_guava_guava",
    ],
)
