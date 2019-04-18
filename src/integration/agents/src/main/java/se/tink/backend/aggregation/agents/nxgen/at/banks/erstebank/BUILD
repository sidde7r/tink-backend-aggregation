java_library(
    name = "erstebank",
    srcs = glob(["**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/api",
        "//src/integration/lib",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/storage",
        "//src/libraries/account_identifier",
        "//src/libraries/amount",
        "//src/libraries/credentials:service",
        "@maven//:com_fasterxml_jackson_core_jackson_annotations",
        "@maven//:com_google_guava_guava",
        "@maven//:com_sun_jersey_jersey_core",
        "@maven//:org_slf4j_slf4j_api",
    ],
)
