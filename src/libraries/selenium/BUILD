java_library(
    name = "selenium",
    srcs = glob(["src/main/**/*.java"]),
    data = [
        "//tools:phantomjs_linux",
        "//tools:phantomjs_mac",
    ],
    visibility = ["//visibility:public"],
    deps = [
        "//third_party:com_google_guava_guava",
        "@selenium//:com_codeborne_phantomjsdriver",
        "@selenium//:org_seleniumhq_selenium_selenium_api",
        "@selenium//:org_seleniumhq_selenium_selenium_htmlunit_driver",
        "@selenium//:org_seleniumhq_selenium_selenium_remote_driver",
    ],
)
