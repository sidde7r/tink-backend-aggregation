load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "v30",
    srcs = glob(["**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/se/banks/nordea/v30",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/banks/nordea/v30",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/exceptions/transfer",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/agents/componentproviders",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/controllers/utils",
        "//src/libraries/i18n_aggregation",
        "//third_party:org_mockito_mockito_core",
    ],
)
