java_library(
    name = "erstebank",
    srcs = glob(["**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/amount",
        "//src/integration/api",
        "//src/integration/credentials:service",
        "//src/integration/lib",
        "//src/integration/lib:annotations",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/storage",
        "//src/libraries/account_identifier",
        "//third_party:com_fasterxml_jackson_core_jackson_annotations",
        "//third_party:com_google_guava_guava",
        "//third_party:com_sun_jersey_jersey_core",
        "//third_party:org_slf4j_slf4j_api",
    ],
)
