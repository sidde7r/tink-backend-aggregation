java_library(
    name = "volksbank",
    srcs = glob(["**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/api",
        "//src/integration/lib",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/annotations",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/storage",
        "//src/libraries/account_identifier",
        "//src/libraries/amount",
        "//src/libraries/credentials:service",
        "@maven//:com_fasterxml_jackson_core_jackson_annotations",
        "@maven//:com_fasterxml_jackson_core_jackson_databind",
        "@maven//:com_sun_jersey_jersey_core",
        "@maven//:joda_time_joda_time",
        "@maven//:org_apache_commons_commons_lang3",
    ],
)
