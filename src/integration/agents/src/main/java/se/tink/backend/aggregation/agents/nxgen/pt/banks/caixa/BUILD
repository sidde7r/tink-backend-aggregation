java_library(
    name = "caixa",
    srcs = glob(["**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/amount",
        "//src/integration/credentials:service",
        "//src/integration/lib",
        "//src/integration/lib:annotations",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/url",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/storage",
        "//src/libraries/account_identifier",
        "//third_party:com_amazonaws_aws_java_sdk_sqs",
        "//third_party:org_slf4j_slf4j_api",
        "@org_apache_commons_commons_collections4//jar",
    ],
)
