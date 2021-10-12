load("@rules_codeowners//tools:codeowners.bzl", "codeowners")
load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "http_api_client",
    srcs = glob(["*.java"]),
    data = [
        "//:agent_test_data",
        "//data:qsealc_crt",
    ],
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/api",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/configuration/eidas",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/configuration/eidas/proxy",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/eidasidentity/identity",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/logmasker",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/controllers/configuration",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http_api_client",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/fakelogmasker",
        "//src/libraries/agent-storage",
        "//src/tink_backend_libs:aggregation_agent_api_client",
        "//third_party:com_github_tomakehurst_wiremock",
        "//third_party:junit_junit",
        "//third_party:lombok_libs",
        "//third_party:org_assertj_assertj_core",
        "//third_party:org_bouncycastle_bcprov_jdk15on",
        "//third_party:org_mockito_mockito_core",
        "@aggregation//:com_fasterxml_jackson_core_jackson_databind",
        "@aggregation//:com_fasterxml_jackson_dataformat_jackson_dataformat_yaml",
    ],
)

codeowners(
    name = "owners",
    teams = [
        "@tink-ab/agent-effectiveness-maintainer",
    ],
    visibility = ["//:__pkg__"],
)
