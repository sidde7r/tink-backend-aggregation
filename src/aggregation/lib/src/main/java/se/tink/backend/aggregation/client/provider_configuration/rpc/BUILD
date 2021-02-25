java_library(
    name = "rpc",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/libraries/date",
        "//src/libraries/serialization",
        "//third_party:com_google_code_findbugs_jsr305",
        "//third_party:io_vavr",
        "//third_party:lombok_libs",
        "@aggregation//:com_fasterxml_jackson_core_jackson_annotations",
        "@aggregation//:com_google_guava_guava",
        "@aggregation//:joda_time_joda_time",
        "@tink_backend//src/libraries/provider",
    ],
)
