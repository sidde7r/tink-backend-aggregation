load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "v62_testRefresh",
    srcs = glob(["AmexV62UkAgentTest.java"]),
    data = [
        "//data:agent_unit_tests",
        "//data:agents",
        "//data:aggregation_test",
        "//data:cryptography_test",
        "//data:security",
        "//data:tesseract_training_set",
        "//etc:development",
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/uk/creditcards/amex/v62/resources",
        "//tools:libkbc_wbaes_linux",
        "//tools:libkbc_wbaes_mac",
        "//tools:phantomjs_linux",
        "//tools:phantomjs_mac",
    ],
    resources = [
        "//src/integration/agents/src/main/resources:config-templates",
    ],
    tags = ["manual"],
    visibility = ["//visibility:public"],
    runtime_deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/uk/creditcards/amex/v62",
    ],
    deps = [
        "//src/integration/api",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework",
    ],
)

junit_test(
    name = "v62_mockServer",
    srcs = glob([
        "AmexV62UkMockServerAgentTest.java",
    ]),
    data = [
        "//data:agent_unit_tests",
        "//data:agents",
        "//data:aggregation_test",
        "//data:cryptography_test",
        "//data:security",
        "//data:tesseract_training_set",
        "//etc:development",
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/uk/creditcards/amex/v62/resources",
        "//tools:libkbc_wbaes_linux",
        "//tools:libkbc_wbaes_mac",
        "//tools:phantomjs_linux",
        "//tools:phantomjs_mac",
    ],
    resources = [
        "//src/integration/agents/src/main/resources:config-templates",
    ],
    visibility = ["//visibility:public"],
    runtime_deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/uk/creditcards/amex/v62",
    ],
    deps = [
        "//src/integration/api",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/framework/wiremock",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/framework/wiremock/configuration",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework/assertions",
        "//src/integration/models",
    ],
)

junit_test(
    name = "v62_mockServerWithContractFile",
    srcs = glob([
        "AmexV62UkMockServerWithContractFileAgentTest.java",
    ]),
    data = [
        "//data:agent_unit_tests",
        "//data:agents",
        "//data:aggregation_test",
        "//data:cryptography_test",
        "//data:security",
        "//data:tesseract_training_set",
        "//etc:development",
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/uk/creditcards/amex/v62/resources",
        "//tools:libkbc_wbaes_linux",
        "//tools:libkbc_wbaes_mac",
        "//tools:phantomjs_linux",
        "//tools:phantomjs_mac",
    ],
    resources = [
        "//src/integration/agents/src/main/resources:config-templates",
    ],
    visibility = ["//visibility:public"],
    runtime_deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/uk/creditcards/amex/v62",
    ],
    deps = [
        "//src/integration/api",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/framework/wiremock",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/framework/wiremock/configuration",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework/assertions",
        "//src/integration/models",
    ],
)
