load("//tools/bzl:junit.bzl", "junit_test")

java_library(
    name = "account_data_cache",
    srcs = glob(["src/main/**/*.java"]),
    visibility = ["//visibility:public"],
    deps = [
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/models",
        "//third_party:com_google_guava_guava",
        "//third_party:org_apache_commons_commons_lang3",
        "//third_party:org_slf4j_slf4j_api",
    ],
)

junit_test(
    name = "account_data_cache_test",
    srcs = glob(["src/test/**/*.java"]),
    deps = [
        "//src/integration/api/src/main/java/se/tink/backend/agents/rpc",
        "//src/integration/models",
        "//src/libraries/account_data_cache",
        "//third_party:junit_junit",
        "//third_party:org_assertj_assertj_core",
        "//third_party:org_mockito_mockito_core",
    ],
)
