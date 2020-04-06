load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "hvb",
    srcs = glob(["HVBAgentTest.java"]),
    data = ["//:agent_test_data"],
    tags = [
        "external",
        "manual",
    ],
    visibility = ["//visibility:public"],
    runtime_deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/de/banks/hvb",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/framework/wiremock/entities",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework/assertions",
        "//src/integration/transfer:mocks",
        "//third_party:pl_pragmatists_junitparams",
    ],
    deps = [
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework",
        "//third_party:com_sun_jersey_jersey_core",
        "//third_party:org_assertj_assertj_core",
        "//third_party:org_hamcrest_hamcrest_core",
        "//third_party:org_hamcrest_hamcrest_library",
        "//third_party:org_mockito_mockito_core",
    ],
)

junit_test(
    name = "hvb_unit_tests",
    srcs = glob(
        ["**/*.java"],
        exclude = ["HVBAgentTest.java"],
    ),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/account/src/main/java/se/tink/libraries/account/enums",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/de/banks/hvb",
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/identity_data",
        "//src/integration/lib",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/client",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/request",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/response",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/url",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/scaffold",
        "//third_party:com_sun_jersey_jersey_core",
        "//third_party:org_assertj_assertj_core",
        "//third_party:org_hamcrest_hamcrest_core",
        "//third_party:org_hamcrest_hamcrest_library",
        "//third_party:org_mockito_mockito_core",
    ],
)
