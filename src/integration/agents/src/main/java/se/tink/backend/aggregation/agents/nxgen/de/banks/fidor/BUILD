java_library(
    name = "fidor",
    srcs = glob(["**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/api",
        "//src/integration/lib",
        "//src/libraries/account_identifier",
        "//src/libraries/amount",
        "//src/libraries/credentials:service",
        "@com_fasterxml_jackson_core_jackson_annotations//jar",
        "@com_github_detro_ghostdriver_phantomjsdriver//jar",
        "@com_sun_jersey_jersey_core//jar",
        "@org_apache_commons_commons_lang3//jar",
        "@org_seleniumhq_selenium_selenium_api//jar",
        "@org_slf4j_slf4j_api//jar",
    ],
)
