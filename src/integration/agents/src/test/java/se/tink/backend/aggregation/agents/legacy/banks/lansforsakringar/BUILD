load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "lansforsakringar",
    srcs = glob(["**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/legacy/banks/lansforsakringar",
        "//src/integration/models",
        "//src/libraries/i18n",
        "//third_party:org_apache_httpcomponents_httpclient",
        "//third_party:org_assertj_assertj_core",
        "@aggregation//:com_fasterxml_jackson_core_jackson_databind",
        "@aggregation//:joda_time_joda_time",
        "@tink_backend//src/libraries/account_identifier",
    ],
)
