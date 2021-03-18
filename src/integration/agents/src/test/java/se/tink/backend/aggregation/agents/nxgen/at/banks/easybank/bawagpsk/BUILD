load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "bawagpsk",
    srcs = glob(["*.java"]),
    data = [
        "//data:aggregation_test",
        "//data:provider_configurations",
        "//etc:development",
    ],
    tags = [
        "external",
        "manual",
    ],
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/at/banks/easybank/bawagpsk",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/at/banks/easybank/bawagpsk/rpc",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/at/banks/easybank/bawagpsk/rpc/entities",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/at/banks/easybank/bawagpsk/rpc/typemappers",
        "//src/integration/amount",
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/lib",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/contexts/agent",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/logmasker",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/storage",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework/context",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/configuration",
        "@aggregation//:com_google_guava_guava",
        "@tink_backend//src/libraries/account_identifier",
    ],
)
