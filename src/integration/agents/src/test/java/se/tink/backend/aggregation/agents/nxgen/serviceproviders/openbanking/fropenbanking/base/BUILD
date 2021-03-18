load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "base",
    srcs = glob(
        ["**/*.java"],
        exclude = ["transfer/**/*.java"],
    ),
    data = ["//:agent_test_data"],
    tags = [
        "external",
        "manual",
    ],
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/fropenbanking/base",
        "//src/integration/amount",
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/lib",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/url",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/storage",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework",
        "//src/integration/payment:enums",
        "//src/integration/payment:rpc",
        "//src/integration/transfer:rpc",
        "//third_party:org_assertj_assertj_core",
        "//third_party:org_iban4j_iban4j",
        "//third_party:org_mockito_mockito_core",
        "@aggregation//:com_google_guava_guava",
        "@tink_backend//src/libraries/account_identifier",
    ],
)
