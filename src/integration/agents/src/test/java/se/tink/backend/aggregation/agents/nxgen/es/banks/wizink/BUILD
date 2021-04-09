load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "wizink",
    srcs = glob(["**/*.java"]),
    data = [
        "//:agent_test_data",
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/es/banks/wizink/resources/authenticator",
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/es/banks/wizink/resources/fetcher/creditcard",
    ],
    tags = [
        "external",
        "manual",
    ],
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/es/banks/wizink",
        "//src/integration/amount",
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/credentials:service",
        "//src/integration/lib",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/client",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/filter/filterable/request",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/response",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/url",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/configuration",
        "//src/libraries/serialization",
        "//third_party:com_fasterxml_jackson_core_jackson_core",
        "//third_party:org_assertj_assertj_core",
        "//third_party:org_mockito_mockito_core",
    ],
)
