load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "fetcher",
    srcs = glob(["**/*.java"]),
    data = [
    ],
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/se/banks/skandiabanken",
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/compliance/account_capabilities",
        "//src/integration/models",
        "//src/libraries/serialization",
        "//third_party:org_assertj_assertj_core",
        "@tink_backend//src/libraries/account_identifier",
    ],
)
