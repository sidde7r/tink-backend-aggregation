load("//tools/bzl:junit_old.bzl", "junit_test")

junit_test(
    name = "amex",
    srcs = glob(["**/*.java"]),
    data = [
        "//data:agent_unit_tests",
        "//data:aggregation_test",
        "//data:cryptography_test",
        "//data:provider_configurations",
        "//etc:development",
    ],
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/se/creditcards/amex/v62",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/creditcards/amex/v62",
        "//src/integration/lib",
        "@aggregation//:com_fasterxml_jackson_core_jackson_databind",
    ],
)
