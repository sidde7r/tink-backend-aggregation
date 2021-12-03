load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "service",
    srcs = glob(["**/*.java"]),
    data = [
        "//tools:phantomjs_linux",
        "//tools:phantomjs_mac",
        "@chromedriver//file",
        "@chromium//file",
    ],
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/utils/log",
        "//src/integration/webdriver/src/main/java/se/tink/integration/webdriver",
        "//src/libraries/agent-storage",
        "//third_party:com_google_guava_guava",
        "//third_party:com_google_inject_guice",
        "//third_party:lombok_libs",
        "//third_party:org_apache_commons_commons_lang3",
        "//third_party:org_slf4j_slf4j_api",
        "//third_party/browserup_proxy_core",
        "@selenium//:org_seleniumhq_selenium_selenium_api",
        "@selenium//:org_seleniumhq_selenium_selenium_java",
    ],
)
