load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "bankid",
    srcs = glob(["**/*.java"]),
    data = [
        "//src/integration/lib/src/test/java/se/tink/backend/aggregation/nxgen/controllers/authentication/multifactor/bankid/screenscraping/resources",
    ],
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/agents/src/main/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/banks/danskebank",
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/lib",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/utils/supplementalfields",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/controllers/authentication/multifactor/bankid/screenscraping",
        "//src/integration/webdriver",
        "//src/libraries/selenium",
        "//third_party:org_assertj_assertj_core",
        "//third_party:org_mockito_mockito_core",
        "//third_party:pl_pragmatists_junitparams",
        "@selenium//:org_seleniumhq_selenium_selenium_api",
    ],
)
