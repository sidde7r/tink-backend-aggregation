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
        "//src/libraries/account:__pkg__",
        "//src/libraries/signableoperation:__pkg__",
    ],
    deps = [
        "//src/integration/transfer:enums",
        "//src/libraries/account_identifier",
        "//src/libraries/amount",
        "//src/libraries/date",
        "//src/libraries/serialization",
        "//src/libraries/uuid",
        "//third_party:com_fasterxml_jackson_core_jackson_annotations",
        "//third_party:com_fasterxml_jackson_core_jackson_core",
        "//third_party:com_fasterxml_jackson_core_jackson_databind",
        "//third_party:com_google_guava_guava",
        "//third_party:org_slf4j_slf4j_api",
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
        "//src/libraries/account_identifier",
        "//src/libraries/amount",
        "//third_party:com_fasterxml_jackson_core_jackson_core",
        "//third_party:com_fasterxml_jackson_core_jackson_databind",
    ],
)
