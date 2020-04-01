load("//tools/bzl:junit.bzl", "junit_test")

java_library(
    name = "api",
    srcs = glob(["src/main/**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/aggregation/lib/src/main/java/se/tink/backend/aggregation/client/provider_configuration/rpc",
        "//src/integration/account/src/main/java/se/tink/libraries/account/enums",
        "//src/integration/amount",
        "//src/integration/credentials:demo",
        "//src/libraries/account_identifier",
        "//src/libraries/serialization",
        "//src/libraries/strings",
        "//src/libraries/uuid",
        "@aggregation//:com_fasterxml_jackson_core_jackson_annotations",
        "@aggregation//:com_fasterxml_jackson_core_jackson_core",
        "@aggregation//:com_google_guava_guava",
    ],
)

junit_test(
    name = "api_test",
    srcs = glob(["src/test/**/*.java"]),
    data = [
    ],
    visibility = ["//visibility:public"],
    deps = [
        "//src/aggregation/lib/src/main/java/se/tink/backend/aggregation/client/provider_configuration/rpc",
        "//src/integration/api",
        "//src/libraries/serialization",
        "//third_party:com_google_inject_guice",
        "//third_party:com_sun_jersey_jersey_core",
        "//third_party:io_dropwizard_dropwizard_configuration",
        "//third_party:javax_validation_validation_api",
        "//third_party:org_assertj_assertj_core",
        "//third_party:org_mockito_mockito_core",
        "@aggregation//:com_fasterxml_jackson_core_jackson_databind",
        "@aggregation//:com_google_guava_guava",
    ],
)
