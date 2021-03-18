load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "bunq",
    srcs = glob(["**/*.java"]),
    data = [
        "//data:aggregation_test",
        "//data:provider_configurations",
        "//etc:development",
    ],
    tags = [
        "external",
        "manual",
    ],
    visibility = ["//visibility:public"],
    deps = [
        "//src/aggregation/api",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/nl/banks/openbanking/bunq",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/tools",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/tools/validator",
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/tools/validator/builder",
        "//src/integration/amount",
        "//src/integration/lib",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/configuration",
        "//third_party:org_assertj_assertj_core",
        "//third_party:org_iban4j_iban4j",
        "//third_party:org_mockito_mockito_core",
        "@aggregation//:com_google_guava_guava",
        "@tink_backend//src/libraries/account_identifier",
    ],
)
