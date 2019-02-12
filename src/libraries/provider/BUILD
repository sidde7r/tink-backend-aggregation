java_library(
    name = "enums",
    srcs = glob(["src/main/**/enums/*.java"]),
    visibility = ["//visibility:public"],
)

java_library(
    name = "rpc",
    srcs = glob(["src/main/**/rpc/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/libraries/credentials:enums",
        "//src/libraries/date",
        "//src/libraries/field:rpc",
        "//src/libraries/provider:enums",
        "//src/libraries/serialization",
        "//third_party:com_fasterxml_jackson_core_jackson_annotations",
        "//third_party:com_google_guava_guava",
        "//third_party:joda_time_joda_time",
    ],
)
