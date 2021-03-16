java_library(
    name = "nextbankid",
    srcs = glob(["**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/lib",
        "//src/integration/webdriver/src/main/java/se/tink/integration/webdriver",
        "//third_party:com_google_code_findbugs_jsr305",
        "//third_party:com_google_inject_guice",
        "//third_party:io_netty_netty_all",
        "//third_party:lombok_libs",
        "//third_party:org_apache_commons_commons_lang3",
        "//third_party:org_slf4j_slf4j_api",
        "//third_party/browserup_proxy_core",
        "@aggregation//:com_google_guava_guava",
        "@selenium//:org_seleniumhq_selenium_selenium_api",
    ],
)
