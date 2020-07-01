load("//tools/bzl:junit_old.bzl", "junit_test")

junit_test(
    name = "revolut",
    srcs = glob(["**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/ukopenbanking/base",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/ukopenbanking/v31",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/uk/openbanking/revolut",
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/ukopenbanking/v31",
        "//src/integration/amount",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/core/account",
        "//src/libraries/mapper",
        "//src/libraries/serialization",
        "//third_party:org_assertj_assertj_core",
        "//third_party:org_mockito_mockito_core",
        "@aggregation//:com_fasterxml_jackson_core_jackson_databind",
        "@aggregation//:com_google_guava_guava",
    ],
)
