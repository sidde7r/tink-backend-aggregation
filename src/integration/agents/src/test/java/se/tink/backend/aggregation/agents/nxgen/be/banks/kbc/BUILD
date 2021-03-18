load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "kbc",
    srcs = glob(["**/*.java"]),
    data = ["//:agent_test_data"],
    tags = [
        "external",
        "manual",
    ],
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/account/src/main/java/se/tink/libraries/account/enums",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/be/banks/kbc",
        "//src/integration/amount",
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/lib",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/utils/crypto",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/utils/encoding",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/configuration",
        "//src/integration/transfer:enums",
        "//src/integration/transfer:rpc",
        "//src/libraries/serialization",
        "//third_party:org_assertj_assertj_core",
        "@aggregation//:com_fasterxml_jackson_core_jackson_databind",
        "@tink_backend//src/libraries/account_identifier",
    ],
)
