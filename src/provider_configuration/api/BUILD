java_library(
    name = "api",
    srcs = glob(["src/main/**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/libraries/api_annotations",
        "//src/libraries/credentials:enums",
        "//src/libraries/field:rpc",
        "//src/libraries/http:http_annotations",
        "//src/libraries/provider:enums",
        "//src/libraries/provider:rpc",
        "//src/libraries/serialization",
        "//third_party:com_fasterxml_jackson_core_jackson_annotations",
        "//third_party:com_google_guava_guava",
        "//third_party:com_sun_jersey_jersey_client",
        "//third_party:com_sun_jersey_jersey_core",
        "//third_party:javax_validation_validation_api",
    ],
)
