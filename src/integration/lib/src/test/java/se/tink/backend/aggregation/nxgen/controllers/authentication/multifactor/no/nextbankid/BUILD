load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "nextbankid",
    srcs = glob(["**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/credentials/src/main/java/se/tink/libraries/credentials/service",
        "//src/integration/lib",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/exceptions/agent",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/utils/supplementalfields",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/controllers/authentication/multifactor/no/nextbankid",
        "//src/integration/webdriver/src/main/java/se/tink/integration/webdriver",
        "//src/integration/webdriver/src/main/java/se/tink/integration/webdriver/service",
        "//third_party:lombok_libs",
        "//third_party:org_apache_commons_commons_lang3",
        "//third_party:org_apache_httpcomponents_httpclient",
        "//third_party:org_assertj_assertj_core",
        "//third_party:org_mockito_mockito_core",
        "//third_party:org_slf4j_slf4j_api",
        "//third_party:pl_pragmatists_junitparams",
        "//third_party/browserup_proxy_core",
        "@aggregation//:com_google_guava_guava",
        "@selenium//:org_seleniumhq_selenium_selenium_api",
    ],
)
