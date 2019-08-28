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
        "//src/integration/account:enums",
        "//src/integration/account:rpc",
        "//src/integration/amount",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/storage",
        "//src/integration/payment:enums",
        "//src/libraries/account_identifier",
        "//src/libraries/date",
        "//src/libraries/pair",
        "//src/libraries/serialization",
        "//src/libraries/uuid",
        "//third_party:com_fasterxml_jackson_core_jackson_annotations",
        "//third_party:com_fasterxml_jackson_core_jackson_core",
        "//third_party:com_fasterxml_jackson_core_jackson_databind",
        "//third_party:com_google_guava_guava",
        "//third_party:org_iban4j_iban4j",
        "//third_party:org_slf4j_slf4j_api",
    ],
)
