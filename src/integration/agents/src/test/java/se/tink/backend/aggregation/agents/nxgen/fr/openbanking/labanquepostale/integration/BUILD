load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "labanquepostale_mock",
    srcs = glob(["*.java"]),
    data = [
        "//data:aggregation_test",
        "//data:provider_configurations",
        "//etc:development",
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/fr/openbanking/labanquepostale/integration/resources",
    ],
    visibility = ["//visibility:public"],
    runtime_deps = ["//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/fr/openbanking/labanquepostale"],
    deps = [
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/fr/openbanking/labanquepostale/integration/module",
        "//src/integration/enums",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/configuration/agentsservice",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework/assertions",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework/compositeagenttest/wiremockrefresh",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/configuration",
        "//third_party:com_github_tomakehurst_wiremock",
        "//third_party:org_mockito_mockito_core",
        "//third_party:pl_pragmatists_junitparams",
        "@aggregation//:com_google_guava_guava",
    ],
)
