java_library(
    name = "embedded",
    srcs = glob(["**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/demo/banks/password",
        "//src/integration/api",
        "//src/integration/credentials:service",
        "//src/integration/lib",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/exceptions/bankservice",
        "//third_party:com_fasterxml_jackson_core_jackson_annotations",
        "//third_party:com_sun_jersey_jersey_core",
        "//third_party:org_apache_commons_commons_lang3",
        "@aggregation//:com_google_guava_guava",
    ],
)
