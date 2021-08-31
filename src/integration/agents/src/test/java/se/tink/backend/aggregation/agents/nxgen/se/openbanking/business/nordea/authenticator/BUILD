load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "authenticator",
    srcs = glob(["*.java"]),
    visibility = ["//visibility:public"],
    runtime_deps = ["//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/se/openbanking/business/nordea"],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/se/openbanking/business/nordea",
        "//src/integration/credentials/src/main/java/se/tink/libraries/credentials/service",
        "//src/integration/enums",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/bankid/status",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/configuration/agentsservice",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/eidassigner",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/agents/componentproviders",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/client",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework/assertions",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework/compositeagenttest/wiremockrefresh",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/configuration",
        "//src/libraries/agent-storage",
        "//third_party:org_mockito_mockito_core",
    ],
)
