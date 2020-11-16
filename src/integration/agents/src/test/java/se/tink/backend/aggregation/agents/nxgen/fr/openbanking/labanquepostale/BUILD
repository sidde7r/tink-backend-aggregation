load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "labanquepostale_unit",
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
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/fr/openbanking/labanquepostale",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/berlingroup",
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/fropenbanking/base/transfer",
        "//src/integration/amount",
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/identity_data",
        "//src/integration/lib",
        "//src/libraries/account_identifier",
        "//src/libraries/mapper",
        "//third_party:com_sun_jersey_jersey_core",
        "//third_party:lombok_libs",
        "//third_party:org_assertj_assertj_core",
        "//third_party:org_hamcrest_hamcrest_library",
        "//third_party:org_mockito_mockito_core",
        "@aggregation//:com_google_guava_guava",
    ],
)
