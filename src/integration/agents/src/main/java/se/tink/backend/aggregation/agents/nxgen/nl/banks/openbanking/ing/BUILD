java_library(
    name = "ing",
    srcs = glob(["**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/ingbase",
        "//src/integration/lib",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/storage",
        "//src/libraries/account_identifier",
        "//src/libraries/amount",
        "//src/libraries/credentials:service",
        "//third_party:com_amazonaws_aws_java_sdk_sqs",
        "@maven//:com_fasterxml_jackson_core_jackson_annotations",
        "@maven//:com_google_code_findbugs_jsr305",
        "@maven//:com_google_guava_guava",
        "@maven//:com_sun_jersey_jersey_core",
        "@maven//:org_assertj_assertj_core",
    ],
)
