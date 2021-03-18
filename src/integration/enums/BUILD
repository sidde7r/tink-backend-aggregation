java_library(
    name = "enums",
    srcs = glob(["src/main/**/*.java"]),
    visibility = [
        "//src/aggregation:__subpackages__",
        "//src/aggregation/aggregationcontroller_api/src/main/java/se/tink/backend/aggregation/aggregationcontroller:__subpackages__",
        "//src/integration:__subpackages__",
    ],
    deps = [
        "//third_party:org_apache_commons_commons_lang3",
        "@aggregation//:com_google_guava_guava",
        "@tink_backend//src/libraries/account_identifier",
    ],
)
