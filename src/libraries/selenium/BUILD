load("//tools/bzl:junit.bzl", "junit_test")

java_library(
    name = "selenium",
    srcs = glob(["src/main/**/*.java"]),
    data = [
        "//tools:phantomjs_linux",
        "//tools:phantomjs_mac",
    ],
    visibility = ["//visibility:public"],
    deps = [
        "@aggregation//:com_google_guava_guava",
        "@selenium//:com_codeborne_phantomjsdriver",
        "@selenium//:org_seleniumhq_selenium_selenium_api",
        "@selenium//:org_seleniumhq_selenium_selenium_htmlunit_driver",
        "@selenium//:org_seleniumhq_selenium_selenium_remote_driver",
        "@selenium//:org_seleniumhq_selenium_selenium_support",
    ],
)

junit_test(
    name = "selenium_test",
    srcs = glob(
        ["src/test/**/*.java"],
    ),
    visibility = ["//visibility:public"],
    deps = [
        "//src/libraries/selenium",
        "//third_party:junit_junit",
        "//third_party:org_assertj_assertj_core",
        "//third_party:org_mockito_mockito_core",
        "@selenium//:com_codeborne_phantomjsdriver",
        "@selenium//:org_seleniumhq_selenium_selenium_api",
    ],
)
