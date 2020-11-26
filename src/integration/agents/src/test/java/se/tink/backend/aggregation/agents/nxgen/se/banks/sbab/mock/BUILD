load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "mock",
    srcs = glob(["*.java"]),
    data = [
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/se/banks/sbab/mock/resources",
    ],
    visibility = ["//visibility:public"],
    runtime_deps = ["//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/se/banks/sbab"],
    deps = [
        "//src/integration/credentials/src/main/java/se/tink/libraries/credentials/service",
        "//src/integration/enums",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework/assertions",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework/compositeagenttest/wiremockrefresh",
        "//third_party:com_github_tomakehurst_wiremock",
        "//third_party:com_google_inject_guice",
        "//third_party:org_mockito_mockito_core",
        "//third_party:pl_pragmatists_junitparams",
        "@aggregation//:com_google_guava_guava",
    ],
)
