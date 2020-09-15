load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "opbank_fetcher_entities",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/fi/openbanking/opbank",
        "//src/integration/amount",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen",
        "//src/libraries/serialization",
        "//third_party:org_assertj_assertj_core",
    ],
)
