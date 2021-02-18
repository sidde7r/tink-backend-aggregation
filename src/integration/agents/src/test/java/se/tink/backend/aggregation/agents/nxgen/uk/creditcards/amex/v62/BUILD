load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "v62_testRefresh",
    srcs = glob(["AmexV62UkAgentTest.java"]),
    data = [
        "//data:agent_unit_tests",
        "//data:agents",
        "//data:aggregation_test",
        "//data:cryptography_test",
        "//data:provider_configurations",
        "//data:security",
        "//data:tesseract_training_set",
        "//etc:development",
        "//tools:libkbc_wbaes_linux",
        "//tools:libkbc_wbaes_mac",
    ],
    tags = ["manual"],
    visibility = ["//visibility:public"],
    runtime_deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/uk/creditcards/amex/v62",
    ],
    deps = [
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework",
    ],
)

junit_test(
    name = "v62_mockServerWithContractFile",
    srcs = glob([
        "AmexV62UkMockServerWithContractFileAgentTest.java",
    ]),
    data = [
        "//data:agents",
        "//data:security",
        "//data:tesseract_training_set",
        "//tools:libkbc_wbaes_linux",
        "//tools:libkbc_wbaes_mac",
    ],
    visibility = ["//visibility:public"],
    runtime_deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/uk/creditcards/amex/v62",
    ],
    deps = [
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/credentials:service",
        "//src/integration/enums",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework/assertions",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework/compositeagenttest/wiremockrefresh",
        "//third_party:com_google_guava_guava",
    ],
)
