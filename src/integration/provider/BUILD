java_library(
    name = "enums",
    srcs = glob(["src/main/**/enums/*.java"]),
    visibility = ["//src/integration:__subpackages__"],
)

java_library(
    name = "rpc",
    srcs = glob(["src/main/**/rpc/*.java"]),
    visibility = ["//src/integration:__subpackages__"],
    deps = [
        "//src/libraries/date",
        "//third_party:com_fasterxml_jackson_core_jackson_annotations",
        "//third_party:com_google_guava_guava",
        "//third_party:joda_time_joda_time",
    ],
)
