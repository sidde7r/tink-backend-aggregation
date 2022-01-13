load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "berlingroup",
    srcs = glob(["**/*.java"]),
    data = [],
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/amount",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/utils/berlingroup",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/utils/berlingroup/payment",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/controllers/payment",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/url",
        "//src/integration/payment:rpc",
        "//src/integration/transfer:rpc",
        "//third_party:org_assertj_assertj_core",
        "//third_party:org_mockito_mockito_core",
        "//third_party:pl_pragmatists_junitparams",
        "@tink_backend//src/libraries/payments_common/src/java/se/tink/libraries/payments/common/model:payments_common_model",
    ],
)
