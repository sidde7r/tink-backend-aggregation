load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "ics",
    srcs = glob(["**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/amount",
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/balance:calculation",
        "//src/integration/models",
        "//src/libraries/account_data_cache",
        "//src/libraries/serialization",
        "//third_party:ch_qos_logback_logback_classic",
        "//third_party:org_apache_commons_commons_lang3",
        "//third_party:org_assertj_assertj_core",
        "//third_party:org_mockito_mockito_core",
        "//third_party:org_slf4j_slf4j_api",
        "//third_party:pl_pragmatists_junitparams",
        "@aggregation//:ch_qos_logback_logback_core",
        "@aggregation//:com_google_guava_guava",
    ],
)
