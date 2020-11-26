load("//tools/bzl:junit_old.bzl", "junit_test")

junit_test(
    name = "ce_integration",
    srcs = glob(["*.java"]),
    data = [
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/fr/banks/caisseepargne/integration/resources",
    ],
    visibility = ["//visibility:public"],
    runtime_deps = ["//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/fr/banks/caisseepargne"],
    deps = [
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/fr/banks/caisseepargne/integration/module",
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/credentials/src/main/java/se/tink/libraries/credentials/service",
        "//src/integration/enums",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework/assertions",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework/compositeagenttest/wiremockrefresh",
        "//third_party:com_github_tomakehurst_wiremock",
        "//third_party:org_mockito_mockito_core",
        "//third_party:pl_pragmatists_junitparams",
        "@aggregation//:com_google_guava_guava",
    ],
)
