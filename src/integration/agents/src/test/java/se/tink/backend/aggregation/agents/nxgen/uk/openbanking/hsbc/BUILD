load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "hsbc",
    srcs = glob(["**/*.java"]),
    data = [
    ],
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/ukopenbanking/common/openid/jwt/signer/iface",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/ukopenbanking/pis",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/uk/openbanking/hsbc",
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/ukopenbanking/pis",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/exceptions/transfer",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/controllers/payment",
        "//src/integration/payment:rpc",
        "//src/integration/transfer:rpc",
        "//third_party:org_assertj_assertj_core",
        "//third_party:org_mockito_mockito_core",
    ],
)
