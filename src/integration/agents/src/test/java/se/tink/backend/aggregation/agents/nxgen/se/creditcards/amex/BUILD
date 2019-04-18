load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "amex",
    srcs = glob(["**/*.java"]),
    data = [
        "//data:agent_unit_tests",
        "//data:aggregation_test",
        "//etc:development",
    ],
    tags = [
        "external",
        "manual",
    ],
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/se/creditcards/amex/v62",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/creditcards/amex/v62",
        "//src/integration/api",
        "//src/integration/lib",
        "//src/integration/lib:lib_test",
        "//src/libraries/serialization",
        "@maven//:com_fasterxml_jackson_core_jackson_databind",
    ],
)
