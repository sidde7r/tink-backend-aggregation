load("//tools/bzl:junit_old.bzl", "junit_test")

junit_test(
    name = "amex",
    srcs = glob(["**/*.java"]),
    data = [
        "//data:cryptography_test",
    ],
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/se/creditcards/amex/v62",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/creditcards/amex/v62",
        "//src/integration/lib",
        "@aggregation//:com_fasterxml_jackson_core_jackson_databind",
    ],
)
