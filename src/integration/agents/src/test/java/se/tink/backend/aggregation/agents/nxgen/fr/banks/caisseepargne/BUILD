load("//tools/bzl:junit_old.bzl", "junit_test")

junit_test(
    name = "caisseepargne",
    srcs = glob(
        ["**/*.java"],
        exclude = [
            "manual/**/*.java",
            "integration/**/*.java",
        ],
    ),
    data = [
        "//data:aggregation_test",
    ],
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/fr/banks/caisseepargne",
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/credentials/src/main/java/se/tink/libraries/credentials/service",
        "//src/integration/identity_data",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/core/account",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework",
        "//src/integration/payment:rpc",
        "//src/libraries/account_identifier",
        "//third_party:org_apache_httpcomponents_httpclient",
        "//third_party:org_assertj_assertj_core",
    ],
)
