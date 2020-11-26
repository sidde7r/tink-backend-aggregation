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
    ],
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/fr/banks/caisseepargne",
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/identity_data",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/core/account",
        "//third_party:org_apache_httpcomponents_httpclient",
        "//third_party:org_assertj_assertj_core",
    ],
)
