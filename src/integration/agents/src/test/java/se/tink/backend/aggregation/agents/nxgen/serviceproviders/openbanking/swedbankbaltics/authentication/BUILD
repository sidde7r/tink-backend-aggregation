load("@rules_codeowners//tools:codeowners.bzl", "codeowners")
load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "authentication",
    srcs = glob(["*/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/agent_sdk/sdk/src/operation",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/swedbank",
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/swedbankbaltics",
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/credentials/src/main/java/se/tink/libraries/credentials/service",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/exceptions",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/controllers/authentication/multifactor/thirdpartyapp/oauth2/constants",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/controllers/authentication/progressive",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/core/authentication",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/http/response",
        "//src/libraries/agent-storage",
        "//third_party:org_assertj_assertj_core",
        "//third_party:org_mockito_mockito_core",
        "@aggregation//:com_fasterxml_jackson_core_jackson_core",
        "@aggregation//:com_fasterxml_jackson_core_jackson_databind",
    ],
)

codeowners(
    name = "owners",
    teams = [
        "@tink-ab/aggregation-thundercats-maintainer",
    ],
    visibility = ["//:__pkg__"],
)
