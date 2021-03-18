load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "lansforsakringar",
    srcs = glob(["**/*.java"]),
    data = ["//:agent_test_data"],
    tags = [
        "external",
        "manual",
    ],
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/legacy/banks/lansforsakringar",
        "//src/integration/amount",
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/credentials/src/main/java/se/tink/libraries/credentials/service",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/utils/transfer",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework/context",
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/framework/legacy",
        "//src/integration/models",
        "//src/integration/transfer:enums",
        "//src/integration/transfer:mocks",
        "//src/integration/transfer:rpc",
        "//src/libraries/i18n",
        "//src/libraries/social_security_number/src/test/java/se/tink/libraries/social/security/ssn",
        "//third_party:org_apache_httpcomponents_httpclient",
        "//third_party:org_assertj_assertj_core",
        "@aggregation//:com_fasterxml_jackson_core_jackson_databind",
        "@aggregation//:joda_time_joda_time",
        "@tink_backend//src/libraries/account_identifier",
        "@tink_backend//src/libraries/account_identifier/src/test/java/se/tink/libraries/account/identifiers/account",
    ],
)
