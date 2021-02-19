load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "novobanco",
    srcs = glob(["**/*.java"]),
    tags = [
    ],
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/pt/banks/novobanco",
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/pt/banks/novobanco/detail",
        "//src/integration/amount",
        "//src/integration/lib",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/storage",
        "//src/integration/models",
        "//src/libraries/account_identifier",
        "//src/libraries/pair",
        "//src/libraries/serialization",
        "//third_party:org_assertj_assertj_core",
        "//third_party:org_mockito_mockito_core",
    ],
)
