load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "monabanq",
    srcs = glob(["**/*.java"]),
    data = [
        "//data:aggregation_test",
        "//data:provider_configurations",
        "//etc:development",
    ],
    tags = [
        "external",
        "manual",
    ],
    visibility = ["//visibility:public"],
    runtime_deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/fr/banks/monabanq",
    ],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/banks/euroinformation",
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/lib",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/configuration",
    ],
)
