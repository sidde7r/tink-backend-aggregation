java_library(
    name = "rpc",
    srcs = glob(["*.java"]),
    visibility = [
        "//src/aggregation:__subpackages__",
        "//src/aggregation/aggregationcontroller_api/src/main/java/se/tink/backend/aggregation/aggregationcontroller:__subpackages__",
        "//src/integration:__subpackages__",
    ],
    deps = [
        "//src/integration/credentials/src/main/java/se/tink/libraries/credentials/enums",
        "//src/libraries/field/src/main/java/se/tink/libraries/field/rpc",
        "//src/libraries/serialization",
        "//third_party:com_fasterxml_jackson_core_jackson_annotations",
        "//third_party:com_fasterxml_jackson_core_jackson_core",
        "//third_party:com_google_guava_guava",
        "@tink_backend//src/aggregation-controller/api/src/main/java/se/tink/backend/aggregationcontroller/v1/rpc/enums",
        "@tink_backend//src/libraries/uuid",
    ],
)
