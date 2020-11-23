load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "handelsbanken",
    srcs = glob(["**/*.java"]),
    data = ["//:agent_test_data"],
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/se/openbanking/handelsbanken",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/handelsbanken",
        "//src/integration/amount",
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/contexts",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/exceptions",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/controllers/payment",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/storage",
        "//src/integration/payment:rpc",
        "//src/integration/transfer:rpc",
        "//src/libraries/account_identifier",
        "//third_party:org_mockito_mockito_core",
    ],
)
