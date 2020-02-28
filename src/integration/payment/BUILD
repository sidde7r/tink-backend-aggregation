java_library(
    name = "enums",
    srcs = glob(["src/main/**/enums/*.java"]),
    visibility = [
        "//src/aggregation:__subpackages__",
        "//src/integration:__subpackages__",
    ],
)

java_library(
    name = "rpc",
    srcs = glob(["src/main/**/rpc/*.java"]),
    visibility = [
        "//src/aggregation:__subpackages__",
        "//src/integration:__subpackages__",
    ],
    deps = [
        "//src/integration/amount",
        "//src/integration/enums",
        "//src/integration/payment:enums",
        "//src/libraries/account_identifier",
        "//src/libraries/pair",
        "//src/libraries/uuid",
        "//third_party:com_fasterxml_jackson_core_jackson_annotations",
        "//third_party:com_fasterxml_jackson_core_jackson_databind",
        "//third_party:org_iban4j_iban4j",
        "//third_party:org_slf4j_slf4j_api",
        "@aggregation//:com_fasterxml_jackson_core_jackson_core",
        "@aggregation//:com_google_guava_guava",
    ],
)
