load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "knab_fetch_unit",
    srcs = glob(["*.java"]),
    data = [
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/nl/openbanking/knab/fetcher/resources",
    ],
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/account/src/main/java/se/tink/libraries/account/enums",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/nl/openbanking/knab",
        "//src/integration/amount",
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/utils/berlingroup",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/core/account",
        "//src/libraries/serialization",
        "//third_party:org_assertj_assertj_core",
        "//third_party:org_mockito_mockito_core",
        "@tink_backend//src/libraries/account_identifier",
    ],
)
