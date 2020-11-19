load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "bec_unit_tests",
    srcs = glob(
        ["**/*.java"],
        exclude = ["agent/*.java"],
    ),
    tags = [
    ],
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/banks/bec",
        "//src/integration/lib",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/controllers/authentication/multifactor/thirdpartyapp/nemid/exception",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/client",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/filter/filterable/request",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/request",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/response",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/url",
        "//src/integration/models",
        "//src/integration/nemid",
        "//src/libraries/serialization",
        "//third_party:com_sun_jersey_jersey_core",
        "//third_party:org_assertj_assertj_core",
        "//third_party:org_mockito_mockito_core",
    ],
)

junit_test(
    name = "bec",
    srcs = glob(["agent/*.java"]),
    data = ["//:agent_test_data"],
    tags = [
        "external",
        "manual",
    ],
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/banks/bec",
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework",
    ],
)
