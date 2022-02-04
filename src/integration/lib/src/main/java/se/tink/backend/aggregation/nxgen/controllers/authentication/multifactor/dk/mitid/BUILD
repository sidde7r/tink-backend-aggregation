load("@rules_java//java:defs.bzl", "java_library")

java_library(
    name = "mitid",
    srcs = glob(["**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/credentials/src/main/java/se/tink/libraries/credentials/service",
        "//src/integration/lib",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/exceptions/mitid",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/utils/log",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/utils/supplementalfields",
        "//src/integration/lib/src/main/java/se/tink/backend/aggregation/annotations",
        "//src/integration/webdriver/src/main/java/se/tink/integration/webdriver/service",
        "//third_party:com_google_guava_guava",
        "//third_party:com_google_inject_guice",
        "//third_party:lombok_libs",
        "//third_party:org_apache_commons_commons_collections4",
        "//third_party:org_apache_commons_commons_lang3",
        "//third_party:org_apache_httpcomponents_httpclient",
        "@aggregation//:com_fasterxml_jackson_core_jackson_databind",
        "@aggregation//:com_google_code_findbugs_jsr305",
        "@org_slf4j_slf4j_api",
        "@selenium//:org_seleniumhq_selenium_selenium_api",
    ],
)
