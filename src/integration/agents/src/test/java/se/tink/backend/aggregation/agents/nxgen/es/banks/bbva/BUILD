load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "bbva",
    srcs = glob(["**/*.java"]),
    data = [
        "//data:agent_unit_tests",
    ],
    visibility = ["//visibility:public"],
    runtime_deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/redsys",
    ],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/es/banks/bbva",
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/redsys",
        "//src/integration/credentials:service",
        "//src/integration/lib",
        "//src/libraries/serialization",
        "//third_party:io_vavr",
        "//third_party:org_mockito_mockito_core",
        "@aggregation//:com_fasterxml_jackson_core_jackson_databind",
    ],
)
