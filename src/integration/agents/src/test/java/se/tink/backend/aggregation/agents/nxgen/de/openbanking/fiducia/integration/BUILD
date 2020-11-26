load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "integration",
    srcs = glob(["*.java"]),
    data = [
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/de/openbanking/fiducia/integration/resources",
    ],
    visibility = ["//visibility:public"],
    runtime_deps = ["//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/de/openbanking/fiducia"],
    deps = [
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/de/openbanking/fiducia/integration/module",
        "//src/integration/enums",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/configuration/agentsservice",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework/assertions",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework/compositeagenttest/wiremockrefresh",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/configuration",
        "//third_party:com_github_tomakehurst_wiremock",
        "//third_party:com_google_inject_guice",
        "//third_party:org_mockito_mockito_core",
        "//third_party:pl_pragmatists_junitparams",
        "@aggregation//:com_google_guava_guava",
    ],
)
