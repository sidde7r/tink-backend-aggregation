load("@tink_backend//tools/bzl:junit_strict.bzl", "junit_test")

junit_test(
    name = "steppable_execution",
    srcs = glob(["*.java"]),
    data = [],
    resources = [],
    deps = [
        "//src/agent_sdk/compatibility_layers/aggregation_service/src/steppable_execution",
        "//src/agent_sdk/runtime/src/steppable_execution",
        "//src/agent_sdk/runtime/src/user_interaction",
        "//src/agent_sdk/sdk/src/steppable_execution",
        "//src/agent_sdk/sdk/src/storage",
        "//src/agent_sdk/sdk/src/user_interaction",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/controllers/utils",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/url",
        "@aggregation//:org_mockito_mockito_core",
    ],
)
