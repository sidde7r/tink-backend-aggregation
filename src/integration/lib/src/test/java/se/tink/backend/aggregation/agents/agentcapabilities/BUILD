load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "agentcapabilities",
    srcs = glob(
        include = ["*.java"],
    ),
    data = [
        "//data:provider_configurations",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/agentfactory/resources",
    ],
    visibility = ["//visibility:public"],
    runtime_deps = [
    ],
    deps = [
        "//src/aggregation/lib/src/main/java/se/tink/backend/aggregation/client/provider_configuration/rpc",
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/enums",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/agent",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/agentcapabilities",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/agentfactory",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/configuration/agentsservice",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/agentfactory/utils",
        "//third_party:junit_junit",
        "//third_party:lombok_libs",
        "//third_party:org_assertj_assertj_core",
        "@aggregation//:com_fasterxml_jackson_core_jackson_annotations",
        "@aggregation//:com_fasterxml_jackson_core_jackson_core",
        "@aggregation//:com_fasterxml_jackson_core_jackson_databind",
    ],
)
