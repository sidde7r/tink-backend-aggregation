java_library(
    name = "document_container",
    srcs = glob(["src/main/**/*.java"]),
    visibility = [
        "//src/aggregation:__subpackages__",
        "//src/aggregation/aggregationcontroller_api/src/main/java/se/tink/backend/aggregation/aggregationcontroller:__subpackages__",
        "//src/integration:__subpackages__",
    ],
    deps = [
        "//third_party:com_sun_jersey_jersey_core",
        "//third_party:commons_io_commons_io",
        "@aggregation//:com_fasterxml_jackson_core_jackson_annotations",
    ],
)
