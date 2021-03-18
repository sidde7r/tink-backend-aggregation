load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "entities",
    srcs = glob(["*.java"]),
    data = [
    ],
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/sibs",
        "//src/integration/amount",
        "//src/integration/lib",
        "//src/integration/payment:rpc",
        "//third_party:junit_junit",
        "//third_party:org_apache_commons_commons_lang3",
        "//third_party:org_assertj_assertj_core",
        "//third_party:org_mockito_mockito_core",
        "//third_party:pl_pragmatists_junitparams",
        "@tink_backend//src/libraries/account_identifier",
    ],
)
