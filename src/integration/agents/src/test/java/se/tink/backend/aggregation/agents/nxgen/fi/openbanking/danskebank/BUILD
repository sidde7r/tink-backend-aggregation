load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "danskebank-agent",
    srcs = glob(["agent/*.java"]),
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
    runtime_deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/fi/openbanking/danskebank",
    ],
    deps = [
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/configuration",
    ],
)

junit_test(
    name = "danskebank",
    srcs = glob(
        ["**/*.java"],
        exclude = ["agent/*.java"],
    ),
    tags = [
        "external",
    ],
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/fi/openbanking/danskebank",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/ukopenbanking/ais/base",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/ukopenbanking/ais/v31",
        "//src/libraries/mapper",
        "//src/libraries/serialization",
        "//third_party:org_assertj_assertj_core",
    ],
)
