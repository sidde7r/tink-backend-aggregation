load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "chebanca",
    srcs = glob(
        ["**/*.java"],
        exclude = ["agent/**/*.java"],
    ),
    data = [
        "//data/test/agents/it/ob/chebanca",
    ],
    tags = [
    ],
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/it/openbanking/chebanca",
        "//src/integration/amount",
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/lib",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/configuration/agents",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/core/authentication",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/response",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/url",
        "//src/libraries/serialization",
        "//third_party:lombok_libs",
        "//third_party:org_assertj_assertj_core",
        "//third_party:org_mockito_mockito_core",
    ],
)
