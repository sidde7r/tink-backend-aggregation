java_library(
    name = "bunq",
    srcs = glob(["**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/api",
        "//src/integration/lib",
        "//src/libraries/amount",
        "//src/libraries/credentials:service",
        "//src/libraries/serialization",
        "//src/libraries/uuid",
        "@com_fasterxml_jackson_core_jackson_annotations//jar",
        "@com_fasterxml_jackson_core_jackson_core//jar",
        "@com_fasterxml_jackson_core_jackson_databind//jar",
        "@com_google_guava_guava//jar",
        "@com_google_http_client_google_http_client//jar",
        "@com_sun_jersey_jersey_core//jar",
    ],
)
