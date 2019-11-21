load("//tools/bzl:junit.bzl", "junit_test")

junit_test(
    name = "phantomjs",
    srcs = glob(["*.java"]),
    data = [],
    visibility = ["//visibility:public"],
    deps = [
        "//third_party:com_github_detro_ghostdriver_phantomjsdriver",
        "@selenium//:org_seleniumhq_selenium_selenium_api",
        "@selenium//:org_seleniumhq_selenium_selenium_java",
    ],
)
