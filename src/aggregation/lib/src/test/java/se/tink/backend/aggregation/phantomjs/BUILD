load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "phantomjs",
    srcs = glob(["*.java"]),
    data = [
        "//tools:phantomjs_linux",
        "//tools:phantomjs_mac",
    ],
    visibility = ["//visibility:public"],
    deps = [
        "@selenium//:com_codeborne_phantomjsdriver",
        "@selenium//:org_seleniumhq_selenium_selenium_api",
        "@selenium//:org_seleniumhq_selenium_selenium_java",
        "@selenium//:org_seleniumhq_selenium_selenium_remote_driver",
    ],
)
