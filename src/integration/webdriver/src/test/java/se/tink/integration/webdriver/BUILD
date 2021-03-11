load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "webdriver_test",
    srcs = glob(["**/*.java"]),
    data = [
        "//tools:phantomjs_linux",
        "//tools:phantomjs_mac",
    ],
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/webdriver/src/main/java/se/tink/integration/webdriver",
        "//third_party:org_assertj_assertj_core",
        "//third_party:org_mockito_mockito_core",
        "@selenium//:com_codeborne_phantomjsdriver",
        "@selenium//:org_seleniumhq_selenium_selenium_api",
        "@selenium//:org_seleniumhq_selenium_selenium_chrome_driver",
        "@selenium//:org_seleniumhq_selenium_selenium_htmlunit_driver",
        "@selenium//:org_seleniumhq_selenium_selenium_java",
        "@selenium//:org_seleniumhq_selenium_selenium_remote_driver",
    ],
)
