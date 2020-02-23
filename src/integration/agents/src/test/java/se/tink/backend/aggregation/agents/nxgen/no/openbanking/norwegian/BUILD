load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "norwegian_unit_tests",
    srcs = glob(
        ["**/*.java"],
        exclude = ["agent/*.java"],
    ),
    data = [
        "//data:aggregation_test",
        "//etc:development",
    ],
    tags = [
    ],
    visibility = ["//visibility:public"],
    runtime_deps = [
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework/assertions",
        "//src/integration/transfer:mocks",
    ],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/no/openbanking/norwegian",
        "//src/integration/lib",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/eidassigner",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/core/authentication",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/request",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/url",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/storage",
        "//src/libraries/serialization",
        "//third_party:com_sun_jersey_jersey_core",
        "//third_party:org_assertj_assertj_core",
        "//third_party:org_mockito_mockito_core",
        "//third_party:pl_pragmatists_junitparams",
    ],
)

junit_test(
    name = "norwegian",
    srcs = glob(
        ["agent/*.java"],
    ),
    data = [
        "//data:aggregation_test",
        "//data:cryptography_test",
        "//etc:development",
    ],
    tags = [
        "external",
        "manual",
    ],
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/no/openbanking/norwegian",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework",
        "//third_party:com_sun_jersey_jersey_core",
        "//third_party:org_mockito_mockito_core",
    ],
)
