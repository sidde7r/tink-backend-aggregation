load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "danskebank",
    srcs = glob(["*.java"]),
    data = [
        "//data:aggregation_test",
        "//data:cryptography_test",
        "//data:provider_configurations",
        "//etc:development",
    ],
    tags = [
        "external",
        "manual",
    ],
    visibility = ["//visibility:public"],
    runtime_deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/no/openbanking/danskebank",
    ],
    deps = [
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/configuration",
    ],
)

junit_test(
    name = "danskebank_no_unit_tests",
    srcs = glob(
        ["**/*.java"],
        exclude = [
            "DanskebankV31NOAgentTest.java",
            "DanskeNoIdentifierFixtures.java",
        ],
    ),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/no/openbanking/danskebank",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/banks/danskebank",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/danskebank",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/ukopenbanking/ais/base",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/ukopenbanking/ais/v31",
        "//src/libraries/mapper",
        "//src/libraries/serialization",
        "//third_party:org_apache_commons_commons_lang3",
        "//third_party:org_assertj_assertj_core",
    ],
)
