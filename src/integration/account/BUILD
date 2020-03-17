java_library(
    name = "enums",
    srcs = glob(["src/main/**/enums/*.java"]),
    visibility = ["//src/integration:__subpackages__"],
    deps = [
        "//src/integration/enums",
    ],
)

java_library(
    name = "rpc",
    srcs = glob(["src/main/**/rpc/*.java"]),
    visibility = [
        "//src/aggregation:__subpackages__",
        "//src/aggregation/aggregationcontroller_api:__subpackages__",
        "//src/integration:__subpackages__",
    ],
    deps = [
        ":enums",
        "//src/integration/account/src/main/java/se/tink/libraries/account/iface",
        "//src/integration/amount",
        "//src/integration/transfer:rpc",
        "//src/libraries/account_identifier",
        "//src/libraries/serialization",
        "//src/libraries/uuid",
        "@aggregation//:com_fasterxml_jackson_core_jackson_annotations",
        "@aggregation//:com_google_guava_guava",
    ],
)
