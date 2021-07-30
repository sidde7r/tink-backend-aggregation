load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "openbank",
    srcs = glob(["**/*.java"]),
    data = [
        "//data:agent_unit_tests",
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/es/banks/openbank/resources",
    ],
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/es/banks/openbank",
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/credentials:service",
        "//src/integration/identity_data",
        "//src/integration/lib",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/exceptions/bankservice",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/request",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/response",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework",
        "//src/libraries/agent-storage",
        "//src/libraries/serialization",
        "//third_party:io_vavr",
        "//third_party:org_assertj_assertj_core",
        "//third_party:org_mockito_mockito_core",
        "@aggregation//:com_fasterxml_jackson_core_jackson_databind",
    ],
)
