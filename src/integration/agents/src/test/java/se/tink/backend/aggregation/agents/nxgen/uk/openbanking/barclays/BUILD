load("//tools/bzl:junit_old.bzl", "junit_test")

junit_test(
    name = "barclays",
    srcs = glob(["**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/ukopenbanking/ais/base",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/ukopenbanking/ais/v31",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/uk/openbanking/barclays",
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//third_party:org_assertj_assertj_core",
        "//third_party:org_mockito_mockito_core",
    ],
)
