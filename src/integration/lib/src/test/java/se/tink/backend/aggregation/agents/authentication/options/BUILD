load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "options",
    srcs = glob(
        include = ["*.java"],
        exclude = ["ValidateAuthenticationOptionsForRealAgentsTest.java"],
    ),
    data = [
        "//data:provider_configurations",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/agentfactory/resources",
    ],
    visibility = ["//visibility:public"],
    runtime_deps = [
    ],
    deps = [
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/agent",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/authentication/options",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/configuration/agentsservice",
        "//third_party:junit_junit",
        "//third_party:lombok_libs",
        "//third_party:org_assertj_assertj_core",
        "@tink_backend//src/libraries/authentication_options",
    ],
)

junit_test(
    name = "options-validation",
    srcs = glob(
        include = ["*.java"],
        exclude = [
            "TestAgent*.java",
            "AuthenticationOptionsExtractorTest.java",
            "BaseTestAgent.java",
        ],
    ),
    data = [
        "//data:provider_configurations",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/agentfactory/resources",
    ],
    visibility = ["//visibility:public"],
    runtime_deps = [
    ],
    deps = [
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/agent",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/agentfactory",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/authentication/options",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/configuration/agentsservice",
        "//third_party:junit_junit",
        "//third_party:lombok_libs",
        "@tink_backend//src/libraries/authentication_options",
    ],
)
