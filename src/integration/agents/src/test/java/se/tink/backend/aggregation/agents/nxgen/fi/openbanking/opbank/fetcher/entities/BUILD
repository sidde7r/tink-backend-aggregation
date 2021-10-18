load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "opbank_fetcher_entities",
    srcs = glob(["*.java"]),
    data = [
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/fi/openbanking/opbank/resources",
    ],
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/fi/openbanking/opbank",
        "//src/integration/amount",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/core/account",
        "//src/integration/models",
        "//src/libraries/serialization",
        "//third_party:org_assertj_assertj_core",
        "@tink_backend//src/libraries/account_identifier",
    ],
)
