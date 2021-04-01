java_library(
    name = "webdriver",
    srcs = glob(["**/*.java"]),
    data = [
        "//tools:phantomjs_linux",
        "//tools:phantomjs_mac",
        "@chromedriver//file",
        "@chromium//file",
    ],
    visibility = ["//visibility:public"],
    deps = [
        "//third_party:com_google_guava_guava",
        "//third_party:lombok_libs",
        "//third_party:org_slf4j_slf4j_api",
        "//third_party/browserup_proxy_core",
        "@selenium//:com_codeborne_phantomjsdriver",
        "@selenium//:org_seleniumhq_selenium_selenium_api",
        "@selenium//:org_seleniumhq_selenium_selenium_chrome_driver",
        "@selenium//:org_seleniumhq_selenium_selenium_htmlunit_driver",
        "@selenium//:org_seleniumhq_selenium_selenium_java",
        "@selenium//:org_seleniumhq_selenium_selenium_remote_driver",
    ],
)
