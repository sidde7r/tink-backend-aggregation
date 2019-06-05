java_library(
    name = "fidor",
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
        "@maven//:com_github_detro_ghostdriver_phantomjsdriver",
        "@maven//:com_sun_jersey_jersey_core",
        "@maven//:org_apache_commons_commons_lang3",
        "@maven//:org_seleniumhq_selenium_selenium_api",
        "@maven//:org_slf4j_slf4j_api",
    ],
)
