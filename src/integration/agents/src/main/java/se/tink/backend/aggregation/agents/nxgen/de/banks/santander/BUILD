java_library(
    name = "santander",
    srcs = glob(["**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/api",
        "//src/integration/lib",
        "//src/libraries/amount",
        "//src/libraries/credentials:service",
        "//src/libraries/serialization",
        "@com_fasterxml_jackson_core_jackson_annotations//jar",
        "@com_fasterxml_jackson_core_jackson_core//jar",
        "@com_fasterxml_jackson_core_jackson_databind//jar",
        "@com_google_guava_guava//jar",
        "@com_sun_jersey_jersey_core//jar",
        "@org_apache_commons_commons_lang3//jar",
        "@org_slf4j_slf4j_api//jar",
    ],
)
