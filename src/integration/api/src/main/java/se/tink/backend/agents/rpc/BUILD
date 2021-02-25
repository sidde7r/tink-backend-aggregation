load("//tools/bzl:junit.bzl", "junit_test")

java_library(
    name = "rpc",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/aggregation/lib/src/main/java/se/tink/backend/aggregation/client/provider_configuration/rpc",
        "//src/integration/account/src/main/java/se/tink/libraries/account/enums",
        "//src/integration/amount",
        "//src/integration/credentials:demo",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/compliance/account_capabilities",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/source_info",
        "//src/libraries/account_identifier",
        "//src/libraries/serialization",
        "//src/libraries/strings",
        "//src/libraries/uuid",
        "//third_party:lombok_libs",
        "//third_party:org_apache_commons_commons_collections4",
        "@aggregation//:com_fasterxml_jackson_core_jackson_annotations",
        "@aggregation//:com_fasterxml_jackson_core_jackson_core",
        "@aggregation//:com_google_guava_guava",
        "@tink_backend//src/aggregation-controller/api/src/main/java/se/tink/backend/aggregationcontroller/v1/rpc/enums",
        "@tink_backend//src/libraries/provider",
    ],
)
