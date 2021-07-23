load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "webtest",
    srcs = glob(["**/*.java"]),
    data = [
        "//data:aggregation_test",
        "//data:cryptography_test",
        "//data:provider_configurations",
        "//etc:development",
    ],
    tags = [
        "external",
        "manual",
    ],
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/credentials:service",
        "//src/integration/lib",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/client",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework",
        "//src/libraries/agent-storage",
        "//third_party:org_mockito_mockito_core",
    ],
)
