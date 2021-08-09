load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "wiremock",
    srcs = glob(["*.java"]),
    data = [
        "//src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/dk/banks/bankdatadk/mock/resources",
    ],
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/banks/bankdata",
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/credentials/src/main/java/se/tink/libraries/credentials/service",
        "//src/integration/enums",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/module/loader",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/configuration/agentsservice",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/agents/componentproviders",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/controllers/authentication/multifactor/nemid",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework/assertions",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework/compositeagenttest/wiremockrefresh",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/configuration",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/nxgen/controllers/authentication/multifactor/nemid/util",
        "//src/libraries/agent-storage",
        "//third_party:lombok_libs",
        "//third_party:org_apache_commons_commons_lang3",
        "//third_party:org_mockito_mockito_core",
        "@aggregation//:com_google_guava_guava",
    ],
)
