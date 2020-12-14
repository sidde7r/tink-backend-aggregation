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
        "//src/aggregation/aggregationcontroller_api:__pkg__",
        "//src/integration:__subpackages__",
        "//src/integration/account:__pkg__",
        "//src/integration/signableoperation:__pkg__",
    ],
    deps = [
        "//src/integration/amount",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/utils",
        "//src/integration/transfer:enums",
        "//src/integration/transfer/src/main/java/se/tink/libraries/transfer/iface",
        "//src/libraries/account_identifier",
        "//src/libraries/date",
        "//src/libraries/serialization",
        "//src/libraries/uuid",
        "//third_party:org_slf4j_slf4j_api",
        "@aggregation//:com_fasterxml_jackson_core_jackson_annotations",
        "@aggregation//:com_fasterxml_jackson_core_jackson_core",
        "@aggregation//:com_fasterxml_jackson_core_jackson_databind",
        "@aggregation//:com_google_guava_guava",
        "@tink_backend//src/libraries/payments_common/src/java/se/tink/libraries/payments/common/model:payments_common_model",
    ],
)

java_library(
    name = "mocks",
    srcs = glob(["src/main/**/mocks/*.java"]),
    visibility = [
        "//src/aggregation:__subpackages__",
        "//src/integration:__subpackages__",
    ],
    deps = [
        ":enums",
        ":rpc",
        "//src/integration/amount",
        "//src/libraries/account_identifier",
        "@aggregation//:com_fasterxml_jackson_core_jackson_core",
        "@aggregation//:com_fasterxml_jackson_core_jackson_databind",
    ],
)
