load("@tink_backend//tools/bzl:junit_strict.bzl", "junit_test")

junit_test(
    name = "payments",
    srcs = glob(["*.java"]),
    data = [],
    resources = [],
    deps = [
        "//src/agent_sdk/compatibility_layers/aggregation_service/src/payments",
        "//src/agent_sdk/runtime/src/environment",
        "//src/agent_sdk/runtime/src/instance",
        "//src/agent_sdk/runtime/src/models/payments",
        "//src/agent_sdk/runtime/test/utils",
        "//src/agent_sdk/sdk/src/payments",
        "//src/agent_sdk/sdk/src/steppable_execution",
        "//src/agent_sdk/sdk/src/storage",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/controllers/utils",
        "@aggregation//:org_hamcrest_hamcrest_core",
        "@aggregation//:org_hamcrest_hamcrest_library",
        "@aggregation//:org_mockito_mockito_core",
    ],
)
