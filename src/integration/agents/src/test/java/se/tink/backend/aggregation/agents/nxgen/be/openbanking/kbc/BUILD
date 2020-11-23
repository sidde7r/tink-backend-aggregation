load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "kbc",
    srcs = glob(["agents/*.java"]),
    data = ["//:agent_test_data"],
    tags = [
        "external",
        "manual",
    ],
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/be/openbanking/kbc",
        "//src/integration/amount",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework",
        "//src/integration/payment:rpc",
        "//src/libraries/account_identifier",
        "//third_party:org_mockito_mockito_core",
    ],
)

junit_test(
    name = "kbc_unit_tests",
    srcs = glob(
        ["**/*.java"],
        exclude = ["agents/**/*.java"],
    ),
    data = [
        "//data:aggregation_test",
        "//data:provider_configurations",
    ],
    visibility = ["//visibility:public"],
    runtime_deps = [
        "//third_party:asm_asm",
    ],
    deps = [
        "//src/aggregation/lib/src/main/java/se/tink/backend/aggregation/agents_platform/agents_framework",
        "//src/aggregation/lib/src/main/java/se/tink/backend/aggregation/agents_platform/framework",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/be/openbanking/kbc",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/berlingroup",
        "//src/integration/amount",
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/credentials/src/main/java/se/tink/libraries/credentials/service",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/agentplatform",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/controllers/authentication/multifactor/thirdpartyapp/oauth2/constants",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/controllers/refresh",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/core/account",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/core/authentication",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/client",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/filter/filterable/request",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/url",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/storage",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/wiremock",
        "//src/libraries/serialization",
        "//third_party:com_github_tomakehurst_wiremock",
        "//third_party:com_sun_jersey_jersey_core",
        "//third_party:org_assertj_assertj_core",
        "//third_party:org_mockito_mockito_core",
        "//third_party:pl_pragmatists_junitparams",
    ],
)
