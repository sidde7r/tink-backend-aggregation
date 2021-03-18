load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "savoie_manual",
    srcs = glob(["**/*.java"]),
    data = ["//:agent_test_data"],
    tags = [
        "external",
        "manual",
    ],
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/fr/openbanking/creditagricole/savoie",
        "//src/integration/amount",
        "//src/integration/lib",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/configuration",
        "//src/integration/payment:rpc",
        "//src/integration/transfer:enums",
        "//src/integration/transfer:rpc",
        "@tink_backend//src/libraries/account_identifier",
    ],
)
