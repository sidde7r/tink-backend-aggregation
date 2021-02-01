load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "transactionalaccount",
    srcs = glob(["**/*.java"]),
    data = [
    ],
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/bnpparibas",
        "//src/integration/amount",
        "//src/integration/identity_data",
        "//src/integration/lib",
        "//src/libraries/serialization",
        "//third_party:org_assertj_assertj_core",
        "//third_party:org_mockito_mockito_core",
    ],
)
