load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "sebopenbanking",
    srcs = glob(["**/*.java"]),
    data = [],
    visibility = ["//visibility:public"],
    runtime_deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/se/openbanking/sebopenbanking",
    ],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/se/openbanking/sebopenbanking",
        "//src/integration/amount",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/exceptions",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/controllers/payment",
        "//src/integration/payment:enums",
        "//src/integration/payment:rpc",
        "//src/integration/transfer:rpc",
        "//src/libraries/account_identifier",
        "//src/libraries/serialization",
        "//third_party:org_mockito_mockito_core",
        "@aggregation//:joda_time_joda_time",
    ],
)
