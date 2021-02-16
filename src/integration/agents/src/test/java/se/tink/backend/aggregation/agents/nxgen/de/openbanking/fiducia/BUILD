load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "fiducia",
    srcs = glob(
        ["**/*.java"],
        exclude = ["agent/*.java"],
    ),
    data = [
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/de/openbanking/fiducia/resources",
    ],
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/de/openbanking/fiducia",
        "//src/integration/amount",
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/lib",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/utils/berlingroup",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/storage",
        "//src/libraries/account_identifier",
        "//src/libraries/serialization",
        "//third_party:org_apache_commons_commons_lang",
        "//third_party:org_assertj_assertj_core",
        "//third_party:org_iban4j_iban4j",
        "//third_party:org_mockito_mockito_core",
        "//third_party:pl_pragmatists_junitparams",
        "@aggregation//:com_google_guava_guava",
    ],
)
