load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "nemid",
    srcs = glob(["**/*.java"]),
    tags = [
        "external",
    ],
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/lib",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/nxgen/controllers/authentication/password/dk/nemid/v2:nemid",
        "//third_party:org_mockito_mockito_core",
        "@org_assertj_assertj_core//jar",
        "@selenium//:com_codeborne_phantomjsdriver",
        "@selenium//:org_seleniumhq_selenium_selenium_api",
    ],
)
