load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "service",
    srcs = glob(["**/*.java"]),
    data = [
        "//src/integration/webdriver/src/test/java/se/tink/integration/webdriver/service/resources",
    ],
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/lib",
        "//src/integration/webdriver/src/main/java/se/tink/integration/webdriver",
        "//src/integration/webdriver/src/main/java/se/tink/integration/webdriver/service",
        "//third_party:lombok_libs",
        "//third_party:org_assertj_assertj_core",
        "//third_party:org_mockito_mockito_core",
        "//third_party:org_slf4j_slf4j_api",
        "//third_party:pl_pragmatists_junitparams",
        "//third_party/browserup_proxy_core",
        "@aggregation//:com_google_guava_guava",
        "@selenium//:org_seleniumhq_selenium_selenium_api",
    ],
)
