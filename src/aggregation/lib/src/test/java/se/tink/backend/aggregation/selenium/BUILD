load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "selenium",
    srcs = glob(["*.java"]),
    data = [],
    visibility = ["//visibility:public"],
    deps = [
        "@selenium//:org_seleniumhq_selenium_selenium_api",
        "@selenium//:org_seleniumhq_selenium_selenium_firefox_driver",
    ],
)
