java_library(
    name = "bankid",
    srcs = glob(["**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/api",
        "//src/integration/credentials:service",
        "//src/integration/identity_data",
        "//src/integration/lib",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/log",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/core/authentication",
        "//src/libraries/account_identifier",
        "//third_party:com_fasterxml_jackson_core_jackson_annotations",
        "//third_party:com_google_guava_guava",
        "//third_party:com_sun_jersey_jersey_core",
        "//third_party:org_apache_commons_commons_lang3",
    ],
)
