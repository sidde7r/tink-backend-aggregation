load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "bankid",
    srcs = glob(["**/*.java"]),
    tags = [
    ],
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/lib",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/utils/supplementalfields",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/controllers/authentication/multifactor/bankid/screenscraping",
        "//src/libraries/selenium",
        "//third_party:org_assertj_assertj_core",
        "//third_party:org_mockito_mockito_core",
        "@selenium//:com_codeborne_phantomjsdriver",
        "@selenium//:org_seleniumhq_selenium_selenium_api",
    ],
)
